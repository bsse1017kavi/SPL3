from django.urls import path
from . import views

urlpatterns = [
    path("", views.simple_upload, name='home'),
    path("select", views.select_tool, name='select'),
    path("result", views.result, name="result"),
    path('loading', views.loading, name="loading"),
    path("process", views.process, name="process")
]