# [Minescript HUD Addon](https://modrinth.com/mod/minescript-hud-addon)

This mod adds the ability to draw custom HUD elements in [Minescript](https://modrinth.com/mod/minescript).

![Example](https://github.com/maksymiblue12/Minescript-Hud-Addon/blob/master/example/example.png)
[Code](https://github.com/maksymiblue12/Minescript-Hud-Addon/blob/master/example/example.py)
<br></br>
## Features
 - Draw ***text*** on the HUD
 - Draw ***rectangles*** and gradient rectangles
 - Render ***items***
 - Render ***textures***
 - Render ***custom shapes***
 - ***Animate*** all element properties
 - Control ***display duration*** and ***render layers***
<br></br>
## Installation
1. Install [Minescript](https://modrinth.com/mod/minescript).
2. Place this addon in your `mods` folder.
3. Launch the game.
<br></br>
## Example
```python
from hud_renderer import *

add_text("Hello World",x=10,y=10,color=argb(255,255,255,255),shadow=True,display_duration=5)
```
<br></br>
## Animations
All HUD elements can be animated using animation functions.
```python
from hud_renderer import *

def move(text:TextObject):
    text.x+=1

_id=add_text("I can MOVE!",x=10,y=10,color=Colors.WHITE,shadow=True,display_duration=5)

animate_text(_id,move)
```
<br></br>
## Mouse Events
All objects can detect mouse hover and click events.
```python
from minescript import echo
from hud_renderer import *

def hover(text:TextObject,mouse:MouseObject,exited:bool):
    if (exited):
        text.color=Colors.WHITE
    else:
        text.color=Colors.GREEN

def click(text:TextObject,mouse:MouseObject):
    echo("Clicked!")

_id=add_text("Click me!",x=10,y=10,color=Colors.WHITE,shadow=True,display_duration=10)

add_mouse_callbacks_and_wait(_id,on_hover=hover,on_click=click)
```
<br></br>
## Layer System
Elements are rendered in layers.
Elements with higher layer values render above elements with lower layers.
<br></br>
## API Overview
> A more detailed API can be found on the [wiki](https://maksymiblue12.github.io/Minescript-Hud-Addon/).
### Text
 - `add_text(...)`
 - `add_text_with_background(...)`
 - `add_advanced_text(...)`
 - `add_advanced_text_with_background(...)`
 - `animate_text(...)`
 - `animate_text_with_background(...)`

### Rectangles
 - `add_rectangle(...)`
 - `add_rectangle_from_corners(...)`
 - `add_gradient_rectangle(...)`
 - `add_stroked_rectangle(...)`
 - `animate_rectangle(...)`

### Items
 - `add_item(...)`
 - `add_advanced_item(...)`
 - `animate_item(...)`

### Textures
> **Note:** Custom textures must be added by a resource pack in the `assets/minescripthud/textures/gui/sprites/` folder.
 - `add_texture(...)`
 - `add_advanced_texture(...)`
 - `animate_texture(...)`

### Shapes
 - `add_shape(...)`
 - `add_advanced_shape(...)`
 - `animate_shape(...)`
 - `add_line(...)`
 - `add_multiline(...)`
 - `add_triangle(...)`
 - `add_quad(...)`
 - `add_circle(...)`
 - `add_ellipse(...)`

### Utility functions
 - `argb(...)`
 - `argb_to_int(...)`
 - `alpha_from_int(...)`
 - `add_async_mouse_callbacks(...)`
 - `add_mouse_callbacks_and_wait(...)`
 - `wait_until_removed(...)`
 - `remove_element(...)`
 - `clear()`
 - `suppress_done_message()`
