from django.shortcuts import render
from django.http import HttpResponse
from django.conf import settings
from django.core.files.storage import FileSystemStorage

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