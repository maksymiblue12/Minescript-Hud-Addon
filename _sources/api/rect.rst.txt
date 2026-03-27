.. py:currentmodule:: draw_text

Rectangle
=========

.. autofunction:: add_rectangle
.. autofunction:: add_rectangle_from_corners
.. autofunction:: animate_rectangle
.. autofunction:: modify_rectangle

----

RectangleObject class
^^^^^^^^^^^^^^^^^^^^^

.. autoclass:: RectangleObject
    :members:
    :member-order: bysource

Example
^^^^^^^

.. code-block:: python3
    :linenos:

    from draw_text import *

    add_rectangle(sx=10, sy=10, w=40, h=10, color=Colors.RED, display_duration=5)
    rect_id = add_rectangle_from_corners(sx=10, sy=40, ex=20, ey=60, color=Colors.YELLOW, display_duration=5)

    animate_text(rect_id, rainbow_animation) # Uses built-in rainbow animation