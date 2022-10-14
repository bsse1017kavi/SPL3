from django.urls import path
from . import views

urlpatterns = [
    path("", views.simple_upload, name='home'),
    path("select", views.select_tool, name='select'),
    path("result", views.result, name="result"),
    path('loading', views.loading, name="loading"),
    path("process", views.process, name="process"),
    path("crypto_report", views.crypto_report, name="crypto_report"),
    path("cogni_report", views.cogni_report, name="cogni_report"),
    path("sec_report", views.sec_report, name="sec_report"),
    path("fl_report", views.fl_report, name="fl_report"),
    path("solutions", views.solutions, name="solutions")
]