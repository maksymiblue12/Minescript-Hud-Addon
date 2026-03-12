.. py:currentmodule:: draw_text

Text
====

.. autofunction:: add_text
.. autofunction:: add_advanced_text
.. autofunction:: animate_text
.. autofunction:: modify_text

----

TextObject class
^^^^^^^^^^^^^^^^

.. autoclass:: TextObject
    :members:
    :member-order: bysource

----

Example
^^^^^^^

.. code-block:: python3
    :linenos:

    from draw_text import *

    add_text("Hello!", x=10, y=10, color=Colors.WHITE, shadow=True, display_duration=5, layer=1)
    add_text("Above Hello!", x=10, y=20, color=Colors.WHITE, shadow=True, display_duration=5, layer=2)

    add_advanced_text("I am BIG!", x=10, y=50, color=Colors.WHITE, shadow=True, display_duration=5, layer=1, matrix=Matrix().scale(2))

    text_id = add_text("I change colors!", x=10, y=80, color=Colors.RED, shadow=False, display_duration=5)

    animate_text(text_id, rainbow_animation) # Uses built-in rainbow animation