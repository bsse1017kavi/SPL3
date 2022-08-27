from django.urls import path
from . import views

urlpatterns = [
    path("", views.simple_upload, name='home'),
    path("select", views.select_tool, name='select'),
    path("result", views.result, name="result"),
    path("test1", views.test1, name="test1"),
    path("test2", views.test2, name="test2")
    #  path('hello', views.select_tool)
]