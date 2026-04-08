from system.lib.hud_renderer import *
from math import radians

clear()

full=argb(255,255,255,255)

add_text("Hello World!",10,10,full,True,10,1)
add_text_with_background("I have a background!",10,25,2,2,full,argb(150,50,50,50),False,10,1)
add_text("This is a rectangle:",10,40,full,False,10,1)
add_rectangle(10,55,50,20,argb(255,255,0,0),10,1)
add_text("This is a gradient rectangle:",10,85,full,False,10,1)
add_gradient_rectangle(10,100,50,20,argb(255,255,0,0),argb(255,250,160,0),10,1)
add_text("This is a diamond:",10,175,full,False,10,1)
add_item("diamond",10,190,10,1)
add_text("This is a minecraft texture (hardcore_full.png):",10,216,full,False,10,1)
add_texture(Identifier("hud/heart/hardcore_full",True),10,231,16,16,1.0,10,1)
add_advanced_text("Elements can be scaled",69,221+16+40,full,False,10,1,Matrix().scale(2))
add_advanced_text("and rotated",10,221+16+40+40+15,full,False,10,1,Matrix().rotate(radians(45)))