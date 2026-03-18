.. py:currentmodule:: draw_text

Texture
=======

.. autofunction:: add_texture
.. autofunction:: add_advanced_texture
.. autofunction:: animate_texture
.. autofunction:: modify_texture

----

TextureObject class
^^^^^^^^^^^^^^^^^^^

.. autoclass:: TextureObject
    :members:
    :member-order: bysource

----

Example
^^^^^^^

.. code-block:: python3
    :linenos:

    from draw_text import *

    def move(t:TextureObject):
        t.x+=1

    # Uses minecraft texture `hardcore_full.png` found in the `assets/minecraft/textures/gui/sprites/hud/heart/` folder.
    add_texture(Identifier("hud/heart/hardcore_full", vanilla=True), x=10, y=16, width=9, height=9, alpha=alpha_from_int(255), display_duration=5)

    # Uses custom texture `red_rectangle.png` added by a resource pack in the `assets/minescripthud/textures/gui/sprites/` folder.
    add_texture(Identifier("rec_rectangle", vanilla=False), x=10, y=32, width=10, height=10, alpha=alpha_from_int(255), display_duration=5)

    # Supports animated textures
    add_texture(Identifier("icon/music_notes", vanilla=True), x=10, y=48, width=16, height=16, alpha=alpha_from_int(255), display_duration=5)

    add_advanced_texture(Identifier("icon/search", vanilla=True), x=10, y=80, width=12, height=12, alpha=alpha_from_int(255), display_duration=5, matrix=Matrix().scale(2))

    texture_id = add_texture(Identifier("icon/accessibility", vanilla=True), x=10, y=160, width=15, height=15, alpha=alpha_from_int(255), display_duration=5)

    animate_texture(texture_id, move)