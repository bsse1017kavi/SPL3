from math import e
from urllib import request
from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.conf import settings
from django.core.files.storage import FileSystemStorage
from django.shortcuts import redirect
import subprocess
import os
import shutil
from django import forms
import re

name = 'hello'
cogni_check = False
crypto_check = False
file_path = ''
file_name = ''
cryptoguard_report_file_name = ''
security_analysis_check = True

def run_source():
    os.environ['ANDROID_HOME'] = '/home/ahmedulkavi/Android/Sdk'
    os.environ['JAVA7_HOME'] = '/usr/lib/jvm/java-8-openjdk-amd64'
    os.environ['JAVA_HOME'] = '/usr/lib/jvm/java-8-openjdk-amd64'

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

def run_cryptoguard():
    global file_path

    cmd = "java"

    cryptoguard_path = 'crypto_tools/cryptoguard.jar'
    apk_path = file_path

    run_source()

    temp = subprocess.Popen([cmd, '-jar', cryptoguard_path, '-in', 'apk', '-s', apk_path, '-m', 'L'], stdout = subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    output = temp.communicate()
    # output = temp.args
    # output = subprocess.list2cmdline(output)

    return output


def check_cryptoguard_violations(report):
    low_weight = 1
    medium_weight = 2
    high_weight = 3

    non_standardised_score = 0

    total_violations = report.count('***Violated Rule ')

    res = [m.start() for m in re.finditer(re.escape('***Violated Rule '), report)]
    violation_count = []
    violation_rule_number = []
    summary_list = []
    summary = ""
    # report.count('***Violated Rule '),
    i = 0
    count = 0
    for elem in res:
        if i+1<len(res):
            count = report.count('***Found:', elem, res[i+1])
        else:
            count = report.count('***Found:', elem)

        violation_count.append(count)
        i+=1
        
    for i in range(0, len(violation_count)-1):
        if violation_count[i] == 0:
            violation_count[i] = 1

    for elem in res:
        i = elem + 17
        num = ""
        while(report[i]!=':'):
            num += report[i]
            i+=1

        num = int(num)
        violation_rule_number.append(num)

    cnt = 0
    for elem in res:
        risk = ''
        if (violation_rule_number[cnt]>=1 and violation_rule_number[cnt]<=7) or violation_rule_number[cnt]==16:
            risk = "High"
            non_standardised_score += high_weight * violation_count[cnt]

        elif violation_rule_number[cnt]>=8 and violation_rule_number[cnt]<=12:
            risk = "Medium"
            non_standardised_score += medium_weight * violation_count[cnt]

        else:
            risk = "Low"
            non_standardised_score += low_weight * violation_count[cnt]

        if risk=="High":
            summary += "<div style=\"color:red;\">"
        elif risk=="Medium":
            summary += "<div style=\"color:orange;\">"
        else:
            summary += "<div style=\"color:black;\">"

        i = elem + 3
        while(report[i]!='*'):
            summary += report[i]
            i+=1

        summary += "Risk: " + risk + "\n"
        summary += str(violation_count[cnt]) + " time(s)\n"
        summary +="</div>"

        summary_list.append(summary)
        summary = ""

        cnt+=1

    score = e ** non_standardised_score

    # score = (e ** non_standardised_score) / ((e ** non_standardised_score)+1)
    # score *= 100

    summary = "<pre style=\"font-family: Garamond, serif;\">"
    summary += "Total violations: " + str(total_violations) + "\n"
    # summary += "Cryptoguard Score: " + str(score) + "%\n\n"
    i = 0
    for elem in summary_list:
        summary += elem
        if i!=len(summary_list)-1:
            summary += "\n"
        i+=1
    summary+="</pre>"

    return summary

    # return res, violation_count, summary_list


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
    global crypto_check, cogni_check, file_name, cryptoguard_report_file_name

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

        # cryptoguard_report_file_name = "_CryptoGuard-04.05.03_avg_f6ca0a1b-de20-445e-914c-8971d4e291bb_.txt"

        with open(cryptoguard_report_file_name, "r") as f:
            cryptoguard_report = f.read()
        
        f.close()

        print(check_cryptoguard_violations(cryptoguard_report))

    if cogni_check:
        path = 'cognicrypt-reports/'
        path += file_name.replace(".apk", "")
        path += "/CogniCrypt-Report.txt"

        print(path)

        with open(path, "r") as f:
            cognicrypt_report = f.read()
        
        f.close()

    cryptoguard_summary = check_cryptoguard_violations(cryptoguard_report)

    context = {
        'cryptoguard_report': cryptoguard_report,
        'cryptoguard_summary': cryptoguard_summary,
        'cognicrypt_report' : cognicrypt_report,
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
    if crypto_check:
        run_cryptoguard()

    if cogni_check:
        run_cognicrypt()
    
    return HttpResponse("")

def loading(request):
    return render(request, 'loading.html')