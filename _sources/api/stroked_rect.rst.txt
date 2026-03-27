.. py:currentmodule:: draw_text

Stroked Rectangle
=================

.. warning::
    Only in versions 1.21.10 and up!

.. autofunction:: add_stroked_rectangle
.. autofunction:: animate_stroked_rectangle
.. autofunction:: modify_stroked_rectangle

----

StrokedRectangleObject class
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. autoclass:: StrokedRectangleObject
    :members:
    :member-order: bysource

Example
^^^^^^^

.. code-block:: python3
    :linenos:

    from draw_text import *

    def move(r:StrokedRectangleObject):
        r.start_x+=1
        r.end_x+=1

    rect_id = add_stroked_rectangle(x=10, y=10, w=50, h=20, color=Colors.RED, display_duration=5)

    animate_stroked_rectangle(rect_id, move)