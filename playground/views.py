from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.conf import settings
from django.core.files.storage import FileSystemStorage
import subprocess
import os
from django import forms

name = 'hello'

def run_source():
    os.environ['ANDROID_HOME'] = '/home/ahmedulkavi/Android/Sdk'
    os.environ['JAVA7_HOME'] = '/usr/lib/jvm/java-8-openjdk-amd64'
    os.environ['JAVA_HOME'] = '/usr/lib/jvm/java-8-openjdk-amd64'

def run_cognicrypt():
    cmd = "java"

    cognicrypt_path = '/home/ahmedulkavi/Desktop/djangoProjects/securityTool/cryptoTools/CryptoAnalysis-Android-1.0/CryptoAnalysis-Android-1.0/CryptoAnalysis-Android-1.0.0-jar-with-dependencies.jar'
    apk = '/home/ahmedulkavi/Desktop/avg.vpn_2.24.5873.apk'
    android_sdk_platform = '/home/ahmedulkavi/Android/Sdk/platforms'
    crypto_api_rules = '/home/ahmedulkavi/Desktop/djangoProjects/securityTool/cryptoTools/crypto-api-rules'

    temp = subprocess.Popen([cmd, '-cp', cognicrypt_path, 'main.CogniCryptAndroid', apk, android_sdk_platform, crypto_api_rules, 'report'])
    temp.communicate()

    # temp = subprocess.Popen([cmd], stdout = subprocess.PIPE)
    # subprocess.Popen(["cd", android_sdk_platform], stdout = subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True).communicate()
    # output = subprocess.Popen(["ls", android_sdk_platform], stdout = subprocess.PIPE).communicate()

def run_cryptoguard():
    cmd = "java"

    # cryptoguard_source_path = "/home/ahmedulkavi/Desktop/djangoProjects/securityTool/cryptoTools/cryptoguard/_cryptoguard.source"

    cryptoguard_path = '/home/ahmedulkavi/Desktop/djangoProjects/securityTool/cryptoTools/cryptoguard/cryptoguard.jar'
    apk_path = '/home/ahmedulkavi/Desktop/avg.vpn_2.24.5873.apk'

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
    if request.method == 'POST' and request.FILES['myfile']:
        myfile = request.FILES['myfile']
        fs = FileSystemStorage()
        filename = fs.save(myfile.name, myfile)
        uploaded_file_url = fs.url(filename)
        return render(request, 'hello.html', {
            'uploaded_file_url': uploaded_file_url
        })
    return render(request, 'hello.html')

def select_tool(request):
    if request.method == 'POST' and request.FILES['myfile']:
        myfile = request.FILES['myfile']
        fs = FileSystemStorage()
        filename = fs.save(myfile.name, myfile)
        uploaded_file_url = fs.url(filename)
        return render(request, 'selector.html', {
            'uploaded_file_url': uploaded_file_url
        })
    return render(request, 'selector.html')

def result (request):
    # run_cognicrypt()

    # temp = subprocess.Popen(['java --version'],stdout = subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    #output = subprocess.Popen(["java -version"], stdout = subprocess.PIPE).communicate()
    # output = temp.communicate()

    # return HttpResponse("Report from Cognicrypt generated")

    #output = run_cryptoguard()

    return HttpResponse()

def test1(request):
    # if this is a POST request we need to process the form data
    if request.method == 'POST':
        # create a form instance and populate it with data from the request:
        form = NameForm(request.POST)
        # check whether it's valid:
        if form.is_valid():
            name = form.cleaned_data['your_name']
            name = "hi"
            # process the data in form.cleaned_data as required
            # ...
            # redirect to a new URL:
            return HttpResponseRedirect('/test2', {'name': name})
    # if a GET (or any other method) we'll create a blank form
    else:
        form = NameForm()

    return render(request, 'test1.html', {'form': form})

def test2(request):
    return render(request, 'test2.html', {'name': name})

class NameForm(forms.Form):
    your_name = forms.CharField(label='Your name', max_length=100)