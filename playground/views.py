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

        print(security_analysis_check)

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

        searchString = "_CryptoGuard-04.05.03_" + file_name.replace(".apk", "")

        for fname in directory:
            if searchString in fname:
                cryptoguard_report_file_name  = fname

        cryptoguard_report = ''

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

    context = {
        'cryptoguard_report': cryptoguard_report,
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