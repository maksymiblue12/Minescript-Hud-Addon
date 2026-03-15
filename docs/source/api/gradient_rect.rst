.. py:currentmodule:: draw_text

Gradient Rectangle
==================

.. autofunction:: add_gradient_rectangle
.. autofunction:: animate_gradient_rectangle
.. autofunction:: modify_gradient_rectangle

----

GradientRectangleObject class
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. autoclass:: GradientRectangleObject
    :members:
    :member-order: bysource

Example
^^^^^^^

.. code-block:: python3
    :linenos:

    from draw_text import *

    def move(r:GradientRectangleObject):
        r.start_x+=1
        r.end_x+=1

    rect_id = add_gradient_rectangle(sx=10, sy=10, w=50, h=20, start_color=Colors.RED, end_color=Colors.YELLOW, display_duration=5)

    animate_gradient_rectangle(rect_id, move)