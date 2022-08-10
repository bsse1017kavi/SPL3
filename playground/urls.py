from django.urls import path
from . import views

urlpatterns = [
    path("", views.simple_upload, name='home'),
    path("select", views.select_tool, name='select')
    #  path('hello', views.select_tool)
]