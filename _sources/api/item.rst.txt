.. py:currentmodule:: draw_text

Item
====

.. autofunction:: add_item
.. autofunction:: add_advanced_item
.. autofunction:: animate_item
.. autofunction:: modify_item

----

ItemObject class
^^^^^^^^^^^^^^^^

.. autoclass:: ItemObject
    :members:
    :member-order: bysource

----

Example
^^^^^^^

.. code-block:: python3
    :linenos:

    from draw_text import *
    from math import radians

    def spin(i:ItemObject):
        i.matrix.rotate(radians(2))

    add_item("stick", x=10, y=10, display_duration=5)
    add_item("minecraft:stick", x=10, y=36, display_duration=5)
    add_advanced_item("netherite_boots[trim={"pattern":"host","material":"emerald"}]", x=10, y=72, display_duration=5, layer=1, matrix=Matrix().scale(2))

    item_id = add_item("emerald", x=10, y=100, display_duration=5)

    animate_item(item_id, spin)