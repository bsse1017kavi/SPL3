import imp
from math import e
from urllib import request
from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.conf import settings
from django.core.files.storage import FileSystemStorage
from django.shortcuts import redirect
import subprocess
import os, re
import shutil
from playground.crypto_logic.cryptoguard import Cryptoguard
from playground.crypto_logic.jadx import decompile
from playground.flow_logic.flowdroid import FlowDroid

from io import BytesIO
from django.template.loader import get_template
from xhtml2pdf import pisa

name = 'hello'
cogni_check = False
crypto_check = False
file_path = ''
file_name = ''
cryptoguard_report_file_name = ''
security_analysis_check = True
sectool_report = ''

cr_rep = ''
co_rep = ''
fl_rep = ''

combined_report = ''

cryptoguard = Cryptoguard()
flowdroid = FlowDroid()

def combine_report(reports):
    global combine_report
    result = ''

    encryption_violations = ["DES"]
    hash_violations = ["MD5","SHA1"]

    for report in reports:
        report = report.replace("SHA-1", "SHA1")

    for report in reports:
        for item in encryption_violations:
            if report.find(item) != -1:
                s = "There is weak encryption function\n"
                if result.find(s) == -1:
                    result += s
                s = item + " is not safe to use\n"
                if result.find(s) == -1:
                    result += s

        # print(result+"$$$")

        for item in hash_violations:
            if report.find(item) != -1:
                s = "There is weak hashing algorithm\n"
                if result.find(s) == -1:
                    result += s
                s = item + " is not safe to use\n"
                if result.find(s) == -1:
                    result += s

        # print(result+"$$$")

    print(result)

    combined_report = result

    return result

def html_to_pdf(template_src, context_dict={}):
     template = get_template(template_src)
     html  = template.render(context_dict)
     result = BytesIO()
     pdf = pisa.pisaDocument(BytesIO(html.encode("ISO-8859-1")), result)
     if not pdf.err:
         return HttpResponse(result.getvalue(), content_type='application/pdf')
     return None

def run_cognicrypt():
    global file_path
    cmd = "java"

    cognicrypt_path = 'crypto_tools/CryptoAnalysis-Android-1.0.0-jar-with-dependencies.jar'
    apk = file_path
    android_sdk_platform = '/home/ahmedulkavi/Android/Sdk/platforms'
    crypto_api_rules = 'crypto_tools/JavaCryptographicArchitecture'

    temp = subprocess.Popen([cmd, '-cp', cognicrypt_path, 'main.CogniCryptAndroid', apk, android_sdk_platform, crypto_api_rules, 'report'])
    temp.communicate()

    # temp = subprocess.Popen([cmd], stdout = subprocess.PIPE)
    # subprocess.Popen(["cd", android_sdk_platform], stdout = subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True).communicate()
    # output = subprocess.Popen(["ls", android_sdk_platform], stdout = subprocess.PIPE).communicate()

def get_cognicrypt_summary(report):
    length = len(report)
    summary = ""
    details = ""
    start = 0
    
    for i in range(0, length):
        if report[i]=='=' and report[i+1]=='=':
            start = i
            break

    for i in range(start,length):
        summary += report[i]

    for i in range(0, start):
        details += report[i]

    error = summary.count("Error")
    # print(error)

    # print(res)

    errors = []
    errorString = ""

    processed_summary = summary.replace("\n", "?")
    processed_summary = summary.replace("\t", "?")
    res = [m.start() for m in re.finditer("Error", processed_summary)]

    for elem in res:
        for i in reversed(range(elem)):
            # print(summary[i])
            if(processed_summary[i]=="?"):
                break
            errorString+=processed_summary[i]
        
        errorString = errorString[::-1]
        errorString += "Error"
        errors.append(errorString)
        errorString = ""

    # print(errors)

    rules = []

    for elem in errors:
        searchString = elem + " violating CrySL rule for "
        index = details.find(searchString)
        print(index)

        target_index = index + len(searchString)
        rule = ""

        for i in range(target_index, len(details)-1):
            if(details[i]=="\n"):
                break
            rule+=details[i]

        rules.append(searchString + rule)

    print(rules)

    i = 0
    for elem in errors:
        summary = summary.replace(elem, rules[i])
        i+=1

    for elem in rules:
        index = summary.find(elem) + len(elem) + 3
        if summary[index+1]=='\n':
            summary = summary[:index] + " time(s)" + summary[index + 1:]
        else:
            summary = summary[:index] + " time(s)\n" + summary[index + 1:]
        


    return summary, details

def combine(request):
    global combined_report

    context = {
        'combined_report': combined_report
    }

    return render(request, 'combine.html', context)

# Create your views here.
def say_hello(request):
    return render(request, 'hello.html')

def simple_upload(request):
    global file_path, file_name, security_analysis_check

    if request.method == 'POST' and request.FILES['myfile']:
        myfile = request.FILES['myfile']
        fs = FileSystemStorage()
        filename = fs.save(myfile.name, myfile)
        uploaded_file_path = fs.path(filename)

        file_name = filename
        file_path = uploaded_file_path

        security_analysis_check = (request.POST.get("flexRadioDefault") == "Crypto")

        # return render(request, 'selector.html', {
        #     'uploaded_file_url': uploaded_file_url
        # })

        if security_analysis_check:
            return HttpResponseRedirect('/select')

        else:
            return HttpResponseRedirect('/loading')

        # os.remove(file_path)


    return render(request, 'hello.html')

def select_tool(request):
    global cogni_check, crypto_check
    if request.method == 'POST':

        cogni_check = request.POST.get("cognicrypt", False)
        crypto_check = request.POST.get("cryptoguard", False)

        return HttpResponseRedirect('/loading')

    return render(request, 'selector.html')

def prepare_report(cryptoguard_summary, cryptoguard_report, cognicrypt_summary, cognicrypt_details, flowdroid_leak_report):
    global cr_rep, co_rep, fl_rep

    cr_rep = ""
    cr_rep += "<h5><b>Report from Cryptoguard</b></h5>"
    cr_rep += cryptoguard_summary + "<br><br>"
    cr_rep += "<pre>"
    cr_rep += cryptoguard_report
    cr_rep += "</pre>"

    co_rep = ""
    co_rep += "<h5><b>Report from Cognicrypt</b></h5>"
    co_rep += "<pre>"
    co_rep += cognicrypt_summary
    co_rep += "</pre>" 
    co_rep += "<br><br>"
    co_rep += "<pre>"
    co_rep += cognicrypt_details
    co_rep += "</pre>"

    fl_rep = ""
    fl_rep += "<h3><b>Report from FlowDroid</b></h5>"
    fl_rep += "<pre>"
    fl_rep += flowdroid_leak_report
    fl_rep += "</pre>"

def result (request):
    global crypto_check, cogni_check, file_name, cryptoguard_report_file_name, cryptoguard, security_analysis_check, sectool_report
    global combined_report

    cryptoguard_report = ''
    cognicrypt_report = ''

    if security_analysis_check:
        if crypto_check:
            directory = os.listdir(".")

            searchString = "_CryptoGuard-04.05.03_"

            for character in file_name:
                if character==".":
                    break
                searchString += character

            for fname in directory:
                if searchString in fname:
                    cryptoguard_report_file_name  = fname

            cryptoguard_report = ''

            # cryptoguard_report_file_name = "_CryptoGuard-04.05.03_avg_44080a74-faa2-430f-8ba2-154aa603e93e_.txt"

            with open(cryptoguard_report_file_name, "r") as f:
                cryptoguard_report = f.read()
            
            f.close()

        if cogni_check:
            path = 'cognicrypt-reports/'
            path += file_name.replace(".apk", "")
            path += "/CogniCrypt-Report.txt"

            print(path)

            with open(path, "r") as f:
                cognicrypt_report = f.read()
            
            f.close()

    cryptoguard_summary = cryptoguard.check_cryptoguard_violations(cryptoguard_report)
    cognicrypt_summary, cognicrypt_details = get_cognicrypt_summary(cognicrypt_report)
    flowdroid_leak_report = ''

    flow_check = not security_analysis_check

    if flow_check:
        flowdroid_leak_report = flowdroid.get_leak_report()

    prepare_report(cryptoguard_summary, cryptoguard_report, cognicrypt_summary, cognicrypt_details, flowdroid_leak_report)

    content = ""
    content += "<pre style=\"font-family: Garamond, serif;font-size: 18px;\">"
    content += sectool_report
    content += "</pre>"

    sectool_report = content

    content = ""

    content += "<pre style=\"font-family: Garamond, serif;font-size: 18px;\">"
    content += cryptoguard_report
    content += "</pre>"

    cryptoguard_report = content

    combined_report = combine_report([cryptoguard_report, cognicrypt_details, sectool_report])

    context = {
        'cryptoguard_report': cryptoguard_report,
        'cryptoguard_summary': cryptoguard_summary,
        'cognicrypt_report' : cognicrypt_details,
        'cognicrypt_summary' : cognicrypt_summary,
        'crypto_check' : crypto_check,
        'flow_check': flow_check,
        'cogni_check' : cogni_check,
        'leak_report' : flowdroid_leak_report,
        'sectool_report': sectool_report
    }

    if crypto_check:
        os.remove(cryptoguard_report_file_name)
    if cogni_check:
        shutil.rmtree('cognicrypt-reports')

    os.remove(file_path)

    return render(request, 'result.html', context)

def process(request):
    global crypto_check, cogni_check, file_path, cryptoguard, security_analysis_check, sectool_report
    if security_analysis_check:
        if crypto_check:
            cryptoguard.run_cryptoguard(file_path)

        if cogni_check:
            run_cognicrypt()

        sectool_report = decompile(file_path)
        # print("a")
        # print(sectool_report)

    else:
        flowdroid.run_flowdroid(file_path)
    
    return HttpResponse("")

def loading(request):
    return render(request, 'loading.html')

def crypto_report(request):
    global cr_rep
    with open("templates/crypto_report.html", "w") as file:
        file.write(cr_rep)
        file.close()

    # getting the template
    pdf = html_to_pdf('crypto_report.html')

    os.remove("templates/crypto_report.html")
        
    # rendering the template
    return HttpResponse(pdf, content_type='application/pdf')

def cogni_report(request):
    global co_rep
    with open("templates/cogni_report.html", "w") as file:
        file.write(co_rep)
        file.close()

    # getting the template
    pdf = html_to_pdf('cogni_report.html')

    os.remove("templates/cogni_report.html")
        
    # rendering the template
    return HttpResponse(pdf, content_type='application/pdf')

def fl_report(request):
    global fl_rep
    with open("templates/fl_report.html", "w") as file:
        file.write(fl_rep)
        file.close()

    # getting the template
    pdf = html_to_pdf('fl_report.html')

    os.remove("templates/fl_report.html")
        
    # rendering the template
    return HttpResponse(pdf, content_type='application/pdf')

def sec_report(request):
    global sectool_report

    content = ""
    content += "<h5><b>Report from SecTool</b></h5>"
    content += "<pre style=\"font-family: Garamond, serif;font-size: 18px;\">"
    content += sectool_report
    content += "</pre>"

    with open("templates/sec_report.html", "w") as file:
        file.write(content)
        file.close()

    # getting the template
    pdf = html_to_pdf('sec_report.html')

    os.remove("templates/sec_report.html")
        
    # rendering the template
    return HttpResponse(pdf, content_type='application/pdf')

def solutions(request):
    return render(request, 'solutions.html')