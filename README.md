# Minescript HUD Addon

This mod adds the ability to draw custom HUD elements in [Minescript](https://modrinth.com/mod/minescript).

![Example](https://github.com/maksymiblue12/Minescript-Hud-Addon/blob/master/example/example.png)
[Code](https://github.com/maksymiblue12/Minescript-Hud-Addon/blob/master/example/example.py)
<br></br>
## Features
 - Draw ***text*** on the HUD
 - Draw ***rectangles*** and gradient rectangles
 - Render ***items***
 - Render ***textures***
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
from draw_text import *

add_text("Hello World",x=10,y=10,color=argb(255,255,255,255),shadow=True,display_duration=5)
```
<br></br>
## Animations
All HUD elements can be animated using animation functions.
```python
from draw_text import *

def move(text:TextObject):
    text.x+=1

_id=add_text("I can MOVE!",x=10,y=10,color=Colors.WHITE,shadow=True,display_duration=5)

animate_text(_id,move)
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

### Utility functions
 - `argb(...)`
 - `argb_to_int(...)`
 - `remove_element(...)`
 - `clear()`
 - `suppress_done_message()`

