from hud_renderer import *
from math import radians

clear()

full=argb(255,255,255,255)

y=10
add_text("Hello World!",10,y,full,True,10,1);y+=15
add_text_with_background("I have a background!",10,y,2,2,full,argb(150,50,50,50),False,10,1);y+=15
add_text("This is a rectangle:",10,y,full,False,10,1);y+=15
add_rectangle(10,y,50,20,argb(255,255,0,0),10,1);y+=30
add_text("This is a gradient rectangle:",10,y,full,False,10,1);y+=15
add_gradient_rectangle(10,y,50,20,argb(255,255,0,0),argb(255,250,160,0),10,1);y+=30
add_text("This is a stroked rectangle:",10,y,full,False,10,1);y+=15
add_stroked_rectangle(10,y,50,20,argb(255,255,0,0),10,1);y+=30
add_text("This is a diamond:",10,y,full,False,10,1);y+=15
add_item("diamond",10,y,10,1);y+=26
add_text("This is a minecraft texture (hardcore_full.png):",10,y,full,False,10,1);y+=15
add_texture(Identifier("hud/heart/hardcore_full",True),10,y,16,16,1.0,10,1);y+=46
add_advanced_text("Elements can be scaled",69,y,full,False,10,1,Matrix().scale(2));y+=55
add_advanced_text("and rotated",10,y,full,False,10,1,Matrix().rotate(radians(45)));y+=50
add_text("and animated!",10,y,full,False,10,1)

y=10
add_text("These are default shapes:",300,y,full,False,10,1);y+=15
add_line((300+5,y),(300+10+5,y+50),2,Colors.RED,10)
add_triangle((300+25+5,y+50),(300+35+5,y+50),(300+30+5,y),Colors.RED,10)
add_quad((300+50+5,y),(300+50+5,y+50),(300+65+5,y+50),(300+65+5,y),Colors.RED,10)
add_circle(300+105+5,y+25,25,Colors.RED,10)
add_ellipse(300+165+5,y+25,25,15,Colors.RED,10)
y+=60
add_text("You can add custom shapes too!",300,y,full,False,10);y+=25
add_text("Shapes blend colors:",300,y,full,False,10);y+=15
add_shape(get_lines_for_triangle(Vertex(300+5,y+25,argb(255,255,70,70)),Vertex(300+5+20,y+50,Colors.YELLOW),Vertex(300+5+35,y+5,Colors.CYAN)),10)
def modifier(v:Vertex,progress:int):
	v.color=argb(255,*map(lambda n:round(n*255),hsv_to_rgb(((progress)%1000)/1000,1,1)))
add_shape(get_lines_for_advanced_circle(300+5+35+15+25,y+25+5,25,Colors.RED,Colors.WHITE,modifier),10)

suppress_done_message()