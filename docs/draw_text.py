from colorsys import rgb_to_hsv,hsv_to_rgb
from collections.abc import Callable


class Colors:
	WHITE=-1
	BLACK=-16777216
	GRAY=-8355712
	DARK_GRAY=-12566464
	LIGHT_GRAY=-6250336
	ALTERNATE_WHITE=-4539718
	RED=-65536
	LIGHT_RED=-2142128
	GREEN=-16711936
	BLUE=-16776961
	YELLOW=-256
	LIGHT_YELLOW=-171
	PURPLE=-11534256
	CYAN=-11010079
	LIGHT_PINK=-13108
	LIGHTER_GRAY=-2039584

class Matrix:
	def __init__(self):
		self._scale=[1,1]
		self._rotate=0
		self._translation=[0,0]

	def scale(self,x,y=None):
		if (y is None): y=x
		self._scale=[self._scale[0]*x,self._scale[1]*y]
		return self

	def rotate(self,radians):
		self._rotate+=radians
		return self

	def translate(self,x,y):
		self._translation=[self._translation[0]+x,self._translation[1]+y]
		return self

	def to_list(self):
		return *self._scale,self._rotate,*self._translation

	@classmethod
	def from_dict(cls,info):
		return cls().scale(info["scale_x"],info["scale_y"]).rotate(info["rotation"]).translate(info["diff_x"],info["diff_y"])

class BaseObject:
	def __init__(self,_id:int):
		self.update(_id)

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		self.display_duration:float=0
		self.layer:int=0
		return True

	def to_list(self):
		return ()

class Identifier:
	def __init__(self,path:str,vanilla:bool):
		self.path=path
		self.vanilla=vanilla

	def to_list(self):
		return self.path,self.vanilla





class TextObject(BaseObject):
	"""
	This class represents a text element. To create a TextObject object, call the constructor with the id of the text element.
	"""
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		#: Text of the text element.
		self.text:str=""

		#: X-coordinate of the text.
		self.x:int=0

		#: Y-coordinate of the text.
		self.y:int=0

		#: Width of the text. This property cannot be assigned.
		self.width:int

		#: Height of the text. This property cannot be assigned.
		self.height:int

		#: Text color. Must be created by the [argb]() function or from the [Colors]() class.
		self.color:int=0

		#: ``True`` if the text has a shadow ``False`` otherwise.
		self.shadow:bool=False

		#: Time in seconds that the element will remain on screen. This property cannot be assigned, use :attr:`display_duration_modifier <TextObject.display_duration_modifier>` to change this value.
		self.display_duration:float

		#: Modifier to the :attr:`display_duration <TextObject.display_duration>` property.
		self.display_duration_modifier:float=0

		#: Layer of the element.
		self.layer:int=1

		#: Transformation matrix applied to the element (scale, rotation, translation).
		self.matrix:Matrix=Matrix()
		self.update(_id)

	@property
	def width(self):
		return self._width

	@property
	def height(self):
		return self._height

	@property
	def display_duration(self):
		return self._display_duration

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		"""
		Updates this TextObject with the values of the text element specified by _id.

		:param _id: ID of the text element.
		"""
		info=get_text_object(_id)
		if (info is None or any(map(lambda x:x is None,info.values()))):
			return False
		self.text:str=info["text"]
		self.x:int=info["x"]
		self.y:int=info["y"]
		self.color:int=info["color"]
		self.shadow:bool=info["shadow"]
		self._display_duration:float=info["displayDuration"]
		self.display_duration_modifier:float=0
		self.layer:int=info["layer"]
		self.matrix=Matrix.from_dict(info)
		self._width=info["width"]
		self._height=info["height"]
		return True

	def to_list(self)->list:
		"""
		Returns a list containing all the values of this TextObject.

		:return: List containing all the values of this TextObject.
		"""
		return [self.text,self.x,self.y,self.color,self.shadow,self.display_duration_modifier,self.layer,self.matrix]


# noinspection PyTypeChecker
def add_text(text:str,x:int,y:int,color:int,shadow:bool,display_duration:float,layer:int=1)->int:
	"""
	Add a text element to the screen.

	:param text: Text to display.
	:param x: X-coordinate of the text position.
	:param y: Y-coordinate of the text position.
	:param color: Text color. Must be created with `argb(...)` or taken from the `Colors` class.
	:param shadow: Whether the text should be rendered with a shadow.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:return: ID of the created element.
	"""
	return (text,x,y,color,shadow,display_duration,layer)

# noinspection PyTypeChecker
def add_advanced_text(text:str,x:int,y:int,color:int,shadow:bool,display_duration:float,layer:int,matrix:Matrix)->int:
	"""
	Add a text element to the screen, with additional scaling, rotation, and translation options.
	
	Advanced version of :func:`add_text` that allows custom transformations.

	:param text: Text to display.
	:param x: X-coordinate of the text position.
	:param y: Y-coordinate of the text position.
	:param color: Text color. Must be created with `argb(...)` or taken from the `Colors` class.
	:param shadow: Whether the text should be rendered with a shadow.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones.
	:param matrix: Transformation matrix applied to the element (scale, rotation, translation).
		See :class:`Matrix`.
	:return: ID of the created element.
	"""
	return (text,x,y,color,shadow,display_duration,layer,*matrix.to_list())

# noinspection PyTypeChecker
def get_text_object(_id:int)->dict:
	return (_id,)

def update_text(_id:int,text:str,x:int,y:int,color:int,shadow:bool,display_duration:float,layer:int,matrix:Matrix):
	return (_id,text,x,y,color,shadow,display_duration,layer,*matrix.to_list())

def _animate_text(_id:int,func:Callable[[TextObject], None])->None:
	t=TextObject(_id)
	while (still_exists(_id)):
		if (not t.update(_id)):
			wait_next_frame()
			continue
		func(t)
		l=t.to_list()
		if (any(x is None for x in l)):
			return
		update_text(_id,*l)
		wait_next_frame()

def animate_text(_id:int,func:Callable[[TextObject],None])->None:
	"""
	Animates the text element with the given id by calling the given function every frame with the text element as the argument.

	:param _id: ID of the text element to animate.
	:param func: Function to use to animate the text element. The function is called every frame with the text element as the argument.
	"""
	_animate_text(_id,func)

def modify_text(_id:int,func:Callable[[TextObject],None])->None:
	"""
	Modifies the text element with the given id by calling the given function once with the text element as the argument.

	:param _id: ID of the text element to modify.
	:param func: Function to use to modify the text element. The function is called on the closest render frame.
	"""
	t=TextObject(_id)
	if (still_exists(_id)):
		if (t.update(_id)):
			func(t)
			update_text(_id,*t.to_list())





class RectangleObject(BaseObject):
	"""
	This class represents a text element. To create a TextObject object, call the constructor with the id of the text element.
	"""
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		#: X-coordinate of the upper-left corner.
		self.start_x:int=0
		#: Y-coordinate of the upper-left corner.
		self.start_y:int=0
		#: X-coordinate of the bottom-right corner.
		self.end_x:int=1
		#: Y-coordinate of the bottom-right corner.
		self.end_y:int=1
		#: Rectangle color. Must be created by the [argb]() function or from the [Colors]() class.
		self.color:int=0
		#: Time in seconds that the element will remain on screen. This property cannot be assigned, use :attr:`display_duration_modifier <RectangleObject.display_duration_modifier>` to change this value.
		self.display_duration:float
		#: Modifier to the :attr:`display_duration <RectangleObject.display_duration>` property.
		self.display_duration_modifier:float=0
		#: Layer of the element.
		self.layer:int=1
		self.update(_id)

	@property
	def display_duration(self):
		return self._display_duration

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		"""
		Updates this RectangleObject with the values of the rectangle element specified by _id.

		:param _id: ID of the rectangle element.
		"""
		info=get_rectangle_object(_id)
		if (info is None or any(map(lambda x:x is None,info.values()))):
			return False
		self.start_x:int=info["sx"]
		self.start_y:int=info["sy"]
		self.end_x:int=info["ex"]
		self.end_y:int=info["ey"]
		self.color:int=info["color"]
		self._display_duration:float=info["displayDuration"]
		self.display_duration_modifier:float=0
		self.layer:int=info["layer"]
		return True

	def to_list(self)->list:
		"""
		Returns a list containing all the values of this RectangleObject.

		:return: List containing all the values of this RectangleObject.
		"""
		return [self.start_x,self.start_y,self.end_x,self.end_y,self.color,self.display_duration_modifier,self.layer]

# noinspection PyTypeChecker
def add_rectangle(sx:int,sy:int,w:int,h:int,color:int,display_duration:float,layer:int=1)->int:
	"""
	Add a rectangle element to the screen.

	:param sx: X-coordinate of the upper-left corner.
	:param sy: Y-coordinate of the upper-left corner.
	:param w: Width of the rectangle.
	:param h: Height of the rectangle.
	:param color: Rectangle color. Must be created with `argb(...)` or taken from the `Colors` class.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:return: ID of the created element.
	"""
	return (sx,sy,sx+w,sy+h,color,display_duration,layer)

# noinspection PyTypeChecker
def add_rectangle_from_corners(sx:int,sy:int,ex:int,ey:int,color:int,display_duration:float,layer:int=1)->int:
	"""
	Add a rectangle element to the screen.

	:param sx: X-coordinate of the upper-left corner.
	:param sy: Y-coordinate of the upper-left corner.
	:param ex: X-coordinate of the bottom-right corner.
	:param ey: Y-coordinate of the bottom-right corner.
	:param color: Rectangle color. Must be created with `argb(...)` or taken from the `Colors` class.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:return: ID of the created element.
	"""
	return (sx,sy,ex,ey,color,display_duration,layer)

# noinspection PyTypeChecker
def get_rectangle_object(_id:int)->dict:
	return (_id,)

def update_rectangle(_id:int,sx:int,sy:int,ex:int,ey:int,color:int,display_duration:float,layer:int):
	return (_id,sx,sy,ex,ey,color,display_duration,layer)

def _animate_rectangle(_id:int,func:Callable[[RectangleObject],None])->None:
	b=RectangleObject(_id)
	while (still_exists(_id)):
		if (b.update(_id)):
			func(b)
			l=b.to_list()
			if (any(map(lambda x:x is None,l))): return
			update_rectangle(_id,*l)
			wait_next_frame()

def animate_rectangle(_id:int,func:Callable[[RectangleObject],None])->None:
	"""
	Animates the rectangle element with the given id by calling the given function every frame with the rectangle element as the argument.

	:param _id: ID of the rectangle element to animate.
	:param func: Function to use to animate the rectangle element. The function is called every frame with the rectangle element as the argument.
	"""
	_animate_rectangle(_id,func)

def modify_rectangle(_id:int,func:Callable[[RectangleObject],None])->None:
	"""
	Modifies the rectangle element with the given id by calling the given function once with the rectangle element as the argument.

	:param _id: ID of the rectangle element to modify.
	:param func: Function to use to modify the rectangle element. The function is called on the closest render frame.
	"""
	b=RectangleObject(_id)
	if (still_exists(_id)):
		if (b.update(_id)):
			func(b)
			update_rectangle(_id,*b.to_list())

class GradientRectangleObject(BaseObject):
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		self.update(_id)

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		info=get_rectangle_object(_id)
		if (info is None or any(map(lambda x:x is None,info.values()))):
			return False
		self.start_x:int=info["sx"]
		self.start_y:int=info["sy"]
		self.end_x:int=info["ex"]
		self.end_y:int=info["ey"]
		self.start_color:int=info["startColor"]
		self.end_color:int=info["endColor"]
		self._display_duration:float=info["displayDuration"]
		self.display_duration_modifier:float=0
		self.layer:int=info["layer"]
		return True

	@property
	def display_duration(self):
		return self._display_duration

	def to_list(self):
		return self.start_x,self.start_y,self.end_x,self.end_y,self.start_color,self.end_color,self.display_duration_modifier,self.layer

# noinspection PyTypeChecker
def add_gradient_rectangle(sx:int,sy:int,w:int,h:int,start_color:int,end_color:int,display_duration:float,layer:int=1)->int:
	return (sx,sy,sx+w,sy+h,start_color,end_color,display_duration,layer)

# noinspection PyTypeChecker
def get_gradient_rectangle_object(_id:int)->dict:
	return (_id,)

def update_gradient_rectangle(_id:int,sx:int,sy:int,ex:int,ey:int,start_color:int,end_color:int,display_duration:float,layer:int):
	return (_id,sx,sy,ex,ey,start_color,end_color,display_duration,layer)

def _animate_gradient_rectangle(_id:int,func:Callable[[GradientRectangleObject],None])->None:
	b=GradientRectangleObject(_id)
	while (still_exists(_id)):
		if (b.update(_id)):
			func(b)
			l=b.to_list()
			if (any(map(lambda x:x is None,l))): return
			update_gradient_rectangle(_id,*l)
			wait_next_frame()

def animate_gradient_rectangle(_id:int,func:Callable[[GradientRectangleObject],None])->None:
	_animate_gradient_rectangle(_id,func)

def modify_gradient_rectangle(_id:int,func:Callable[[GradientRectangleObject],None])->None:
	b=GradientRectangleObject(_id)
	if (still_exists(_id)):
		if (b.update(_id)):
			func(b)
			update_gradient_rectangle(_id,*b.to_list())

class StrokedRectangleObject(BaseObject):
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		self.update(_id)

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		info=get_rectangle_object(_id)
		if (info is None or any(map(lambda x:x is None,info.values()))):
			return False
		self.x:int=info["x"]
		self.y:int=info["y"]
		self.width:int=info["width"]
		self.height:int=info["height"]
		self.color:int=info["color"]
		self._display_duration:float=info["displayDuration"]
		self.display_duration_modifier:float=0
		self.layer:int=info["layer"]
		return True

	@property
	def display_duration(self):
		return self._display_duration

	def to_list(self):
		return self.x,self.y,self.width,self.height,self.color,self.display_duration_modifier,self.layer

# noinspection PyTypeChecker
def add_stroked_rectangle(x:int,y:int,w:int,h:int,color:int,display_duration:float,layer:int=1)->int:
	return (x,y,w,h,color,display_duration,layer)

# noinspection PyTypeChecker
def get_stroked_rectangle_object(_id:int)->dict:
	return (_id,)

def update_stroked_rectangle(_id:int,x:int,y:int,w:int,h:int,color:int,display_duration:float,layer:int):
	return (_id,x,y,w,h,color,display_duration,layer)

def _animate_stroked_rectangle(_id:int,func:Callable[[StrokedRectangleObject],None])->None:
	b=StrokedRectangleObject(_id)
	while (still_exists(_id)):
		if (b.update(_id)):
			func(b)
			l=b.to_list()
			if (any(map(lambda x:x is None,l))): return
			update_stroked_rectangle(_id,*l)
			wait_next_frame()

def animate_stroked_rectangle(_id:int,func:Callable[[StrokedRectangleObject],None])->None:
	_animate_stroked_rectangle(_id,func)

def modify_stroked_rectangle(_id:int,func:Callable[[StrokedRectangleObject],None])->None:
	b=StrokedRectangleObject(_id)
	if (still_exists(_id)):
		if (b.update(_id)):
			func(b)
			update_stroked_rectangle(_id,*b.to_list())





class TextWithBackgroundObject(BaseObject):
	"""
	This class represents a text with background element. To create a TextWithBackgroundObject object, call the constructor with the id of the text with background element.
	"""
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		#: Text of the text element.
		self.text:str=""

		#: X-coordinate of the text.
		self.x:int=0

		#: Y-coordinate of the text.
		self.y:int=0

		#: Width of the text. This property cannot be assigned.
		self.width:int

		#: Height of the text. This property cannot be assigned.
		self.height:int

		#: Horizontal padding between the text and the background box.
		self.margin_x:int=0

		#: Vertical padding between the text and the background box.
		self.margin_y:int=0

		#: Text color. Must be created by the [argb]() function or from the [Colors]() class.
		self.color:int=0

		#: Background color. Must be created with `argb(...)` or taken from the `Colors` class.
		self.bg_color:int=0

		#: ``True`` if the text has a shadow ``False`` otherwise.
		self.shadow:bool=False

		#: Time in seconds that the text will remain on screen. This property cannot be assigned, use :attr:`display_duration_modifier <TextWithBackgroundObject.display_duration_modifier>` to change this value.
		self.display_duration:float

		#: Modifier to the :attr:`display_duration <TextWithBackgroundObject.display_duration>` property.
		self.display_duration_modifier:float=0

		#: Layer of the element.
		self.layer:int=1

		#: Transformation matrix applied to the element (scale, rotation, translation).
		self.matrix:Matrix=Matrix()
		self.update(_id)

	@property
	def width(self):
		return self._width

	@property
	def height(self):
		return self._height

	@property
	def display_duration(self):
		return self._display_duration

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		"""
		Updates this TextObject with the values of the text element specified by _id.

		:param _id: ID of the text element.
		"""
		info=get_text_with_background_object(_id)
		if (info is None or any(map(lambda x:x is None,info.values()))):
			return False
		self.text:str=info["text"]
		self.x:int=info["x"]
		self.y:int=info["y"]
		self.margin_x=info["marginX"]
		self.margin_y=info["marginY"]
		self.color:int=info["color"]
		self.bg_color:int=info["bgColor"]
		self.shadow:bool=info["shadow"]
		self._display_duration:float=info["displayDuration"]
		self.display_duration_modifier:float=0
		self.layer:int=info["layer"]
		self.matrix=Matrix.from_dict(info)
		self._width=info["width"]
		self._height=info["height"]
		return True

	def to_list(self)->list:
		"""
		Returns a list containing all the values of this TextWithBackgroundObject.

		:return: List containing all the values of this TextWithBackgroundObject.
		"""
		return [self.text,self.x,self.y,self.margin_x,self.margin_y,self.color,self.bg_color,self.shadow,self.display_duration_modifier,self.layer,self.matrix]

# noinspection PyTypeChecker
def add_text_with_background(text:str,x:int,y:int,margin_x:int,margin_y:int,color:int,bg_color:int,shadow:bool,display_duration:float,layer:int=1)->int:
	"""
	Add a text element with a background to the screen.

	:param text: Text to display.
	:param x: X-coordinate of the text position.
	:param y: Y-coordinate of the text position.
	:param margin_x: Horizontal padding between the text and the background box.
	:param margin_y: Vertical padding between the text and the background box.
	:param color: Text color. Must be created with `argb(...)` or taken from the `Colors` class.
	:param bg_color: Background color. Must be created with `argb(...)` or taken from the `Colors` class.
	:param shadow: Whether the text should be rendered with a shadow.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:return: ID of the created element.
	"""
	return (text,x,y,margin_x,margin_y,color,bg_color,shadow,display_duration,layer)

# noinspection PyTypeChecker
def add_advanced_text_with_background(text:str,x:int,y:int,margin_x:int,margin_y:int,color:int,bg_color:int,shadow:bool,display_duration:float,layer:int,matrix:Matrix)->int:
	"""
	Add a text element with a background to the screen, with additional scaling, rotation, and translation options.

	Advanced version of :func:`add_text_with_background` that allows custom transformations.

	:param text: Text to display.
	:param x: X-coordinate of the text position.
	:param y: Y-coordinate of the text position.
	:param margin_x: Horizontal padding between the text and the background box.
	:param margin_y: Vertical padding between the text and the background box.
	:param color: Text color. Must be created with `argb(...)` or taken from the `Colors` class.
	:param bg_color: Background color. Must be created with `argb(...)` or taken from the `Colors` class.
	:param shadow: Whether the text should be rendered with a shadow.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones.
	:param matrix: Transformation matrix applied to the element (scale, rotation, translation).
		See :class:`Matrix`.
	:return: ID of the created element.
	"""
	return (text,x,y,margin_x,margin_y,color,bg_color,shadow,display_duration,layer,*matrix.to_list())

# noinspection PyTypeChecker
def get_text_with_background_object(_id:int)->dict:
	return (_id,)

def update_text_with_background(_id:int,text:str,x:int,y:int,margin_x:int,margin_y:int,color:int,bg_color:int,shadow:bool,display_duration:float,layer:int,matrix:Matrix):
	return (_id,text,x,y,margin_x,margin_y,color,bg_color,shadow,display_duration,layer,*matrix.to_list())

def _animate_text_with_background(_id:int,func:Callable[[TextWithBackgroundObject],None])->None:
	t=TextWithBackgroundObject(_id)
	while (still_exists(_id)):
		if (t.update(_id)):
			func(t)
			l=t.to_list()
			if (any(map(lambda x:x is None,l))): return
			update_text_with_background(_id,*l)
			wait_next_frame()

def animate_text_with_background(_id:int,func:Callable[[TextWithBackgroundObject],None])->None:
	"""
	Animates the text with background element with the given id by calling the given function every frame with the text with background element as the argument.

	:param _id: ID of the text element to animate.
	:param func: Function to use to animate the text element. The function is called every frame with the text with background element as the argument.
	"""
	_animate_text_with_background(_id,func)

def modify_text_with_background(_id:int,func:Callable[[TextWithBackgroundObject],None])->None:
	"""
	Modifies the text with background element with the given id by calling the given function once with the text with background element as the argument.

	:param _id: ID of the text element to modify.
	:param func: Function to use to modify the text element. The function is called on the closest render frame.
	"""
	t=TextWithBackgroundObject(_id)
	if (still_exists(_id)):
		if (t.update(_id)):
			func(t)
			update_text_with_background(_id,*t.to_list())





class ItemObject(BaseObject):
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		self.update(_id)

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		info=get_item_object(_id)
		if (info is None or any(map(lambda x:x is None,info.values()))):
			return False
		self.item:str=info["item"]
		self.x:int=info["x"]
		self.y:int=info["y"]
		self._display_duration:float=info["displayDuration"]
		self.display_duration_modifier:float=0
		self.layer:int=info["layer"]
		self.matrix=Matrix.from_dict(info)
		return True

	@property
	def display_duration(self):
		return self._display_duration

	def to_list(self):
		return self.item,self.x,self.y,self.display_duration_modifier,self.layer,self.matrix

# noinspection PyTypeChecker
def add_item(item:str,x:int,y:int,display_duration:float,layer:int=1)->int:
	return (item,x,y,display_duration,layer)

# noinspection PyTypeChecker
def add_advanced_item(item:str,x:int,y:int,display_duration:float,layer:int,matrix:Matrix)->int:
	return (item,x,y,display_duration,layer,*matrix.to_list())

# noinspection PyTypeChecker
def get_item_object(_id:int)->dict:
	return (_id,)

def update_item(_id:int,item:str,x:int,y:int,display_duration:float,layer:int,matrix:Matrix):
	return (_id,item,x,y,display_duration,layer,*matrix.to_list())

def _animate_item(_id:int,func:Callable[[ItemObject],None])->None:
	t=ItemObject(_id)
	while (still_exists(_id)):
		if (t.update(_id)):
			func(t)
			l=t.to_list()
			if (any(map(lambda x:x is None,l))): return
			update_item(_id,*l)
			wait_next_frame()

def animate_item(_id:int,func:Callable[[ItemObject],None])->None:
	_animate_item(_id,func)

def modify_item(_id:int,func:Callable[[ItemObject],None])->None:
	t=ItemObject(_id)
	if (still_exists(_id)):
		if (t.update(_id)):
			func(t)
			update_item(_id,*t.to_list())





class TextureObject(BaseObject):
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		self.update(_id)

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		info=get_texture_object(_id)
		if (info is None or any(map(lambda x:x is None,info.values()))):
			return False
		self.texture:Identifier=Identifier(info["texture"],info["vanilla"])
		self.x:int=info["x"]
		self.y:int=info["y"]
		self.width:int=info["width"]
		self.height:int=info["height"]
		self.alpha:float=info["alpha"]
		self._display_duration:float=info["displayDuration"]
		self.display_duration_modifier:float=0
		self.layer:int=info["layer"]
		self.matrix=Matrix.from_dict(info)
		return True

	@property
	def display_duration(self):
		return self._display_duration

	def to_list(self):
		return self.texture,self.x,self.y,self.width,self.height,self.alpha,self.display_duration_modifier,self.layer,self.matrix

# noinspection PyTypeChecker
def add_texture(texture:Identifier,x:int,y:int,width:int,height:int,alpha:float,display_duration:float,layer:int=1)->int:
	return (*texture.to_list(),x,y,width,height,alpha,display_duration,layer)

# noinspection PyTypeChecker
def add_advanced_texture(texture:Identifier,x:int,y:int,width:int,height:int,alpha:float,display_duration:float,layer:int,matrix:Matrix)->int:
	return (*texture.to_list(),x,y,width,height,alpha,display_duration,layer,*matrix.to_list())

# noinspection PyTypeChecker
def get_texture_object(_id:int)->dict:
	return (_id,)

def update_texture(_id:int,texture:Identifier,x:int,y:int,width:int,height:int,alpha:float,display_duration:float,layer:int,matrix:Matrix):
	return (_id,*texture.to_list(),x,y,width,height,alpha,display_duration,layer,*matrix.to_list())

def _animate_texture(_id:int,func:Callable[[TextureObject],None])->None:
	t=TextureObject(_id)
	while (still_exists(_id)):
		if (t.update(_id)):
			func(t)
			l=t.to_list()
			if (any(map(lambda x:(x is None),l))): return
			update_texture(_id,*l)
			wait_next_frame()

def animate_texture(_id:int,func:Callable[[TextureObject],None])->None:
	_animate_texture(_id,func)

def modify_texture(_id:int,func:Callable[[TextureObject],None])->None:
	t=TextureObject(_id)
	if (still_exists(_id)):
		if (t.update(_id)):
			func(t)
			update_texture(_id,*t.to_list())





class MouseObject:
	def __init__(self,mouse:dict):
		self.x=mouse["x"]
		self.y=mouse["y"]
		self.was_pressed_left=mouse["left"]
		self.was_pressed_middle=mouse["middle"]
		self.was_pressed_right=mouse["right"]

def rainbow_animation(t:BaseObject,step:int=1):
	a,r,g,b=argb_to_int(t.color)
	h,s,v=rgb_to_hsv(r/255,g/255,b/255)
	h+=step/255
	if (h>255):h=0
	elif (h<0): h=255
	r,g,b=hsv_to_rgb(h,s,v)
	t.color=argb(a,int(r*255),int(g*255),int(b*255))

# noinspection PyTypeChecker
def rainbow_animation_with_speed(step:int)->Callable[[BaseObject,int],None]:
	if (step>255 or step<-255):
		raise ValueError("Step must be between -255 and 255")
	return lambda t:rainbow_animation(t,step)

def alpha_from_int(alpha:int)->float:
	if (alpha<0 or alpha>255):
		raise ValueError("Alpha must be between 0 and 255!")
	return alpha/255

def argb(a:int,r:int,g:int,b:int)->int:
	if (a>255 or a<0 or r>255 or r<0 or g>255 or g<0 or b>255 or b<0):
		raise ValueError(f"a,r,g,b values must be between 0 and 255!")
	value=(a<<24)|(r<<16)|(g<<8)|b
	if (value>=0x80000000):
		value-=0x100000000
	return value

def argb_to_int(color:int)->tuple[int,int,int,int]:
	a=(color>>24)&0xFF
	r=(color>>16)&0xFF
	g=(color>>8)&0xFF
	b=color&0xFF
	return (a,r,g,b)

def remove_element(_id:int):
	return (_id,)

def still_exists(_id:int):
	return (_id,)

def _get_mouse():
	return ()

def get_mouse():
	return MouseObject(_get_mouse())

def get_font_height():
	return ()

def clear():
	return ()

def wait_next_frame():
	return ()

def suppress_done_message():
	return ()