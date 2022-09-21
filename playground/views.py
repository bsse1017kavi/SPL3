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

name = 'hello'
cogni_check = False
crypto_check = False
file_path = ''
file_name = ''
cryptoguard_report_file_name = ''
security_analysis_check = True

cryptoguard = Cryptoguard()

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

        os.remove(file_path)


    return render(request, 'hello.html')

def select_tool(request):
    global cogni_check, crypto_check
    if request.method == 'POST':

        cogni_check = request.POST.get("cognicrypt", False)
        crypto_check = request.POST.get("cryptoguard", False)

        return HttpResponseRedirect('/loading')

    return render(request, 'selector.html')

def result (request):
    global crypto_check, cogni_check, file_name, cryptoguard_report_file_name, cryptoguard

    cryptoguard_report = ''
    cognicrypt_report = ''

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

    context = {
        'cryptoguard_report': cryptoguard_report,
        'cryptoguard_summary': cryptoguard_summary,
        'cognicrypt_report' : cognicrypt_details,
        'cognicrypt_summary' : cognicrypt_summary,
        'crypto_check' : crypto_check,
        'cogni_check' : cogni_check
    }

    if crypto_check:
        os.remove(cryptoguard_report_file_name)
    if cogni_check:
        shutil.rmtree('cognicrypt-reports')

    os.remove(file_path)

    return render(request, 'result.html', context)

def process(request):
    global crypto_check, cogni_check, file_path, cryptoguard
    if crypto_check:
        cryptoguard.run_cryptoguard(file_path)

    if cogni_check:
        run_cognicrypt()
    
    return HttpResponse("")

def loading(request):
    return render(request, 'loading.html')