.. py:currentmodule:: draw_text

Text with Background
====================

.. autofunction:: add_text_with_background
.. autofunction:: add_advanced_text_with_background
.. autofunction:: animate_text_with_background
.. autofunction:: modify_text_with_background

----

TextWithBackgroundObject class
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. autoclass:: TextWithBackgroundObject
    :members:
    :member-order: bysource

----

Example
^^^^^^^

.. code-block:: python3
    :linenos:

    from draw_text import *

    bg=argb(150,50,50,50)

    add_text_with_background("Hello, I have a background!", x=10, y=10, color=Colors.WHITE, bg_color=bg, shadow=True, display_duration=5, layer=1)
    add_text_with_background("Above Hello!", x=10, y=20, color=Colors.WHITE, bg_color=bg, shadow=True, display_duration=5, layer=2)

    add_advanced_text_with_background("I am BIG!", x=10, y=50, color=Colors.WHITE, bg_color=bg, shadow=True, display_duration=5, layer=1, matrix=Matrix().scale(2))

    text_id = add_text_with_background("I change colors!", x=10, y=80, color=Colors.RED, bg_color=bg, shadow=False, display_duration=5)

    animate_text_with_background(text_id, rainbow_animation) # Uses built-in rainbow animation