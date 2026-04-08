from colorsys import rgb_to_hsv,hsv_to_rgb
from minescript_runtime import ScriptFunction,NoReturnScriptFunction
from collections.abc import Callable
from math import radians,sin,cos,ceil,hypot


class Colors:
	"""
	Collection of built-in minecraft colors.
	"""
	#: White color.
	WHITE=-1
	#: Black color.
	BLACK=-16777216
	#: Gray color.
	GRAY=-8355712
	#: Dark gray color.
	DARK_GRAY=-12566464
	#: Light gray color.
	LIGHT_GRAY=-6250336
	#: Alternate white color.
	ALTERNATE_WHITE=-4539718
	#: Red color.
	RED=-65536
	#: Light red color.
	LIGHT_RED=-2142128
	#: Green color.
	GREEN=-16711936
	#: Blue color.
	BLUE=-16776961
	#: Yellow color.
	YELLOW=-256
	#: Light yellow color.
	LIGHT_YELLOW=-171
	#: Purple color.
	PURPLE=-11534256
	#: Cyan color.
	CYAN=-11010079
	#: Light pink color.
	LIGHT_PINK=-13108
	#: Lighter gray color.
	LIGHTER_GRAY=-2039584

# noinspection PyUnresolvedReferences
class Matrix:
	"""
	Represents a 2D matrix that supports scaling, rotation and translation operations.
	"""
	def __init__(self):
		self._scale=[1,1]
		self._rotate=0
		self._translation=[0,0]

	def scale(self,x,y=None):
		"""
		Applies scaling to the matrix.

		:param x: Scaling factor along x-axis.
		:param y: Scaling factor along y-axis. If not provided, `x` is used.
		:return: This matrix.
		"""
		if (y is None): y=x
		self._scale=[self._scale[0]*x,self._scale[1]*y]
		return self

	def rotate(self,radians):
		"""
		Applies rotation to the matrix.

		:param radians: Angle to rotate in radians.
		:return: This matrix.
		"""
		self._rotate+=radians
		return self

	def translate(self,x,y):
		"""
		Applies translation to the matrix.

		:param x: Translation along x-axis.
		:param y: Translation along y-axis.
		:return: This matrix.
		"""
		self._translation=[self._translation[0]+x,self._translation[1]+y]
		return self

	def to_list(self)->list:
		"""
		Converts this matrix into a list.

		:return: List containing all elements of this matrix.
		"""
		return [*self._scale,self._rotate,*self._translation]

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
	"""
	Represents a texture identifier.

	:param path: Path to the texture starting from the ``textures/gui/sprites/`` folder.
	:param vanilla: ``True`` if the texture is a vanilla texture, ``False`` otherwise.
	"""
	def __init__(self,path:str,vanilla:bool):
		self.path=path
		self.vanilla=vanilla

	def to_list(self)->list:
		return [self.path,self.vanilla]

class Vertex:
	def __init__(self,x:int,y:int,color:int):
		self.x=x
		self.y=y
		self.color=color

	def to_dict(self)->dict:
		return {"x":self.x,"y":self.y,"color":self.color}

class Line:
	def __init__(self,start:Vertex,end:Vertex):
		self.start=start
		self.end=end

	def to_list(self)->list:
		return [self.start.to_dict(),self.end.to_dict()]


def update_batch(data):
	return (data,)
update_batch=NoReturnScriptFunction("batch_update",update_batch)

class BatchAnimator:
	def __init__(self):
		self.animations=[]

	def animate_text(self,_id,func):
		self.animations.append({"id":_id,"func":func,"type":"text","update_func":update_text,"object_type":TextObject,"object":None})
		return self

	def animate_rectangle(self,_id,func):
		self.animations.append({"id":_id,"func":func,"type":"rectangle","update_func":update_rectangle,"object_type":RectangleObject,"object":None})
		return self

	def animate_gradient_rectangle(self,_id,func):
		self.animations.append({"id":_id,"func":func,"type":"gradient_rectangle","update_func":update_gradient_rectangle,"object_type":GradientRectangleObject,"object":None})
		return self

	def animate_stroked_rectangle(self,_id,func):
		self.animations.append({"id":_id,"func":func,"type":"stroked_rectangle","update_func":update_stroked_rectangle,"object_type":StrokedRectangleObject,"object":None})
		return self

	def animate_text_with_background(self,_id,func):
		self.animations.append({"id":_id,"func":func,"type":"text_with_background","update_func":update_text_with_background,"object_type":TextWithBackgroundObject,"object":None})
		return self

	def animate_item(self,_id,func):
		self.animations.append({"id":_id,"func":func,"type":"item","update_func":update_item,"object_type":ItemObject,"object":None})
		return self

	def animate_texture(self,_id,func):
		self.animations.append({"id":_id,"func":func,"type":"texture","update_func":update_texture,"object_type":TextureObject,"object":None})
		return self

	def animate_shape(self,_id,func):
		self.animations.append({"id":_id,"func":func,"type":"shape","update_func":update_shape,"object_type":ShapeObject,"object":None})
		return self

	def start(self):
		while (len(self.animations)>0):
			data=[]
			for anim in self.animations:
				_id,func,update_func,obj=anim["id"],anim["func"],anim["update_func"],anim["object"]
				if (not still_exists(_id)):
					continue
				if (obj is None):
					obj=anim["object_type"](_id)
					anim["object"]=obj
				if (not obj.update(_id)):
					continue
				func(obj)
				l=obj.to_list()
				if (any(x is None for x in l)): continue
				for i in range(len(l)):
					if (hasattr(l[i],"to_list")):
						l[i:i+1]=l[i].to_list()
				data.append({"id":_id,"type":anim["type"],"data":l})
			update_batch(data)
			self.animations=[a for a in self.animations if still_exists(a["id"])]
			wait_next_frame()





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
add_text=ScriptFunction("add_text",add_text)

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
add_advanced_text=ScriptFunction("add_advanced_text",add_advanced_text)

# noinspection PyTypeChecker
def get_text_object(_id:int)->dict:
	return (_id,)
get_text_object=ScriptFunction("get_text_object",get_text_object)

def update_text(_id:int,text:str,x:int,y:int,color:int,shadow:bool,display_duration:float,layer:int,matrix:Matrix):
	return (_id,text,x,y,color,shadow,display_duration,layer,*matrix.to_list())
update_text=NoReturnScriptFunction("update_text",update_text)

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
add_rectangle=ScriptFunction("add_rectangle",add_rectangle)

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
add_rectangle_from_corners=ScriptFunction("add_rectangle_from_corners",add_rectangle_from_corners)

# noinspection PyTypeChecker
def get_rectangle_object(_id:int)->dict:
	return (_id,)
get_rectangle_object=ScriptFunction("get_rectangle_object",get_rectangle_object)

def update_rectangle(_id:int,sx:int,sy:int,ex:int,ey:int,color:int,display_duration:float,layer:int):
	return (_id,sx,sy,ex,ey,color,display_duration,layer)
update_rectangle=NoReturnScriptFunction("update_rectangle",update_rectangle)

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
		#: X-coordinate of the upper-left corner.
		self.start_x:int=0
		#: Y-coordinate of the upper-left corner.
		self.start_y:int=0
		#: X-coordinate of the bottom-right corner.
		self.end_x:int=1
		#: Y-coordinate of the bottom-right corner.
		self.end_y:int=1
		#: Color at the top of the rectangle. Must be created by the [argb]() function or from the [Colors]() class.
		self.start_color:int=0
		#: Color at the bottom of the rectangle. Must be created by the [argb]() function or from the [Colors]() class.
		self.end_color:int=0
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
		Updates this GradientRectangleObject with the values of the gradient rectangle element specified by _id.

		:param _id: ID of the gradient rectangle element.
		"""
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

	def to_list(self)->list:
		"""
		Returns a list containing all the values of this GradientRectangleObject.

		:return: List containing all the values of this GradientRectangleObject.
		"""
		return [self.start_x,self.start_y,self.end_x,self.end_y,self.start_color,self.end_color,self.display_duration_modifier,self.layer]

# noinspection PyTypeChecker
def add_gradient_rectangle(sx:int,sy:int,w:int,h:int,start_color:int,end_color:int,display_duration:float,layer:int=1)->int:
	"""
	Add a gradient rectangle element to the screen.

	:param sx: X-coordinate of the upper-left corner.
	:param sy: Y-coordinate of the upper-left corner.
	:param w: Width of the rectangle.
	:param h: Height of the rectangle.
	:param start_color: Color at the top of the rectangle. Must be created by the [argb]() function or from the [Colors]() class.
	:param end_color: Color at the bottom of the rectangle. Must be created by the [argb]() function or from the [Colors]() class.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:return: ID of the created element.
	"""
	return (sx,sy,sx+w,sy+h,start_color,end_color,display_duration,layer)
add_gradient_rectangle=ScriptFunction("add_gradient_rectangle",add_gradient_rectangle)

# noinspection PyTypeChecker
def get_gradient_rectangle_object(_id:int)->dict:
	return (_id,)
get_gradient_rectangle_object=ScriptFunction("get_gradient_rectangle_object",get_gradient_rectangle_object)

def update_gradient_rectangle(_id:int,sx:int,sy:int,ex:int,ey:int,start_color:int,end_color:int,display_duration:float,layer:int):
	return (_id,sx,sy,ex,ey,start_color,end_color,display_duration,layer)
update_gradient_rectangle=NoReturnScriptFunction("update_gradient_rectangle",update_gradient_rectangle)

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
	"""
	Animates the gradient rectangle element with the given id by calling the given function every frame with the gradient rectangle element as the argument.

	:param _id: ID of the gradient rectangle element to animate.
	:param func: Function to use to animate the gradient rectangle element. The function is called every frame with the gradient rectangle element as the argument.
	"""
	_animate_gradient_rectangle(_id,func)

def modify_gradient_rectangle(_id:int,func:Callable[[GradientRectangleObject],None])->None:
	"""
	Modifies the gradient rectangle element with the given id by calling the given function once with the gradient rectangle element as the argument.

	:param _id: ID of the gradient rectangle element to modify.
	:param func: Function to use to modify the gradient rectangle element. The function is called on the closest render frame.
	"""
	b=GradientRectangleObject(_id)
	if (still_exists(_id)):
		if (b.update(_id)):
			func(b)
			update_gradient_rectangle(_id,*b.to_list())

class StrokedRectangleObject(BaseObject):
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		#: X-coordinate of the upper-left corner.
		self.x:int=0
		#: Y-coordinate of the upper-left corner.
		self.y:int=0
		#: Width of the rectangle.
		self.width:int
		#: Height of the rectangle.
		self.height:int
		#: Rectangle color. Must be created by the [argb]() function or from the [Colors]() class.
		self.color:int=0
		#: Time in seconds that the element will remain on screen. This property cannot be assigned, use :attr:`display_duration_modifier <StrokedRectangleObject.display_duration_modifier>` to change this value.
		self.display_duration:float
		#: Modifier to the :attr:`display_duration <StrokedRectangleObject.display_duration>` property.
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
		Updates this StrokedRectangleObject with the values of the rectangle element specified by _id.

		:param _id: ID of the rectangle element.
		"""
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

	def to_list(self)->list:
		"""
		Returns a list containing all the values of this StrokedRectangleObject.

		:return: List containing all the values of this StrokedRectangleObject.
		"""
		return [self.x,self.y,self.width,self.height,self.color,self.display_duration_modifier,self.layer]

# noinspection PyTypeChecker
def add_stroked_rectangle(x:int,y:int,w:int,h:int,color:int,display_duration:float,layer:int=1)->int:
	"""
	Add a stroked (outline) rectangle element to the screen.

	:param x: X-coordinate of the upper-left corner.
	:param y: Y-coordinate of the upper-left corner.
	:param w: Width of the rectangle.
	:param h: Height of the rectangle.
	:param color: Rectangle color. Must be created with `argb(...)` or taken from the `Colors` class.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:return: ID of the created element.
	"""
	return (x,y,w,h,color,display_duration,layer)
add_stroked_rectangle=ScriptFunction("add_stroked_rectangle",add_stroked_rectangle)

# noinspection PyTypeChecker
def get_stroked_rectangle_object(_id:int)->dict:
	return (_id,)
get_stroked_rectangle_object=ScriptFunction("get_stroked_rectangle_object",get_stroked_rectangle_object)

def update_stroked_rectangle(_id:int,x:int,y:int,w:int,h:int,color:int,display_duration:float,layer:int):
	return (_id,x,y,w,h,color,display_duration,layer)
update_stroked_rectangle=NoReturnScriptFunction("update_stroked_rectangle",update_stroked_rectangle)

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
	"""
	Animates the stroked rectangle element with the given id by calling the given function every frame with the stroked rectangle element as the argument.

	:param _id: ID of the stroked rectangle element to animate.
	:param func: Function to use to animate the stroked rectangle element. The function is called every frame with the stroked rectangle element as the argument.
	"""
	_animate_stroked_rectangle(_id,func)

def modify_stroked_rectangle(_id:int,func:Callable[[StrokedRectangleObject],None])->None:
	"""
	Modifies the stroked rectangle element with the given id by calling the given function once with the stroked rectangle element as the argument.

	:param _id: ID of the stroked rectangle element to modify.
	:param func: Function to use to modify the stroked rectangle element. The function is called on the closest render frame.
	"""
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
add_text_with_background=ScriptFunction("add_text_with_background",add_text_with_background)

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
add_advanced_text_with_background=ScriptFunction("add_advanced_text_with_background",add_advanced_text_with_background)

# noinspection PyTypeChecker
def get_text_with_background_object(_id:int)->dict:
	return (_id,)
get_text_with_background_object=ScriptFunction("get_text_with_background_object",get_text_with_background_object)

def update_text_with_background(_id:int,text:str,x:int,y:int,margin_x:int,margin_y:int,color:int,bg_color:int,shadow:bool,display_duration:float,layer:int,matrix:Matrix):
	return (_id,text,x,y,margin_x,margin_y,color,bg_color,shadow,display_duration,layer,*matrix.to_list())
update_text_with_background=NoReturnScriptFunction("update_text_with_background",update_text_with_background)

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
		#: Item to display. Uses the `/give <https://minecraft.wiki/w/Commands/give>`_ command `format <https://minecraft.wiki/w/Argument_types#item_stack>`_.
		self.item:str=""
		#: X-coordinate of the item.
		self.x:int=0
		#: Y-coordinate of the item.
		self.y:int=0
		#: Time in seconds that the element will remain on screen. This property cannot be assigned, use :attr:`display_duration_modifier <ItemObject.display_duration_modifier>` to change this value.
		self.display_duration:float
		#: Modifier to the :attr:`display_duration <ItemObject.display_duration>` property.
		self.display_duration_modifier:float=0
		#: Layer of the element.
		self.layer:int=1
		#: Transformation matrix applied to the element (scale, rotation, translation).
		self.matrix:Matrix=Matrix()
		self.update(_id)

	@property
	def display_duration(self):
		return self._display_duration

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		"""
		Updates this ItemObject with the values of the item element specified by _id.

		:param _id: ID of the item element.
		"""
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

	def to_list(self)->list:
		"""
		Returns a list containing all the values of this ItemObject.

		:return: List containing all the values of this ItemObject.
		"""
		return [self.item,self.x,self.y,self.display_duration_modifier,self.layer,self.matrix]

# noinspection PyTypeChecker
def add_item(item:str,x:int,y:int,display_duration:float,layer:int=1)->int:
	"""
	Add an item element to the screen.

	:param item: Item to display. Uses the `/give <https://minecraft.wiki/w/Commands/give>`_ command `format <https://minecraft.wiki/w/Argument_types#item_stack>`_.
	:param x: X-coordinate of the item position.
	:param y: Y-coordinate of the item position.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:return: ID of the created element.
	"""
	return (item,x,y,display_duration,layer)
add_item=ScriptFunction("add_item",add_item)

# noinspection PyTypeChecker
def add_advanced_item(item:str,x:int,y:int,display_duration:float,layer:int,matrix:Matrix)->int:
	"""
	Add an item element to the screen, with additional scaling, rotation, and translation options.

	Advanced version of :func:`add_item` that allows custom transformations.

	:param item: Item to display. Uses the `/give <https://minecraft.wiki/w/Commands/give>`_ command `format <https://minecraft.wiki/w/Argument_types#item_stack>`_.
	:param x: X-coordinate of the item position.
	:param y: Y-coordinate of the item position.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:param matrix: Transformation matrix applied to the element (scale, rotation, translation).
		See :class:`Matrix`.
	:return: ID of the created element.
	"""
	return (item,x,y,display_duration,layer,*matrix.to_list())
add_advanced_item=ScriptFunction("add_advanced_item",add_advanced_item)

# noinspection PyTypeChecker
def get_item_object(_id:int)->dict:
	return (_id,)
get_item_object=ScriptFunction("get_item_object",get_item_object)

def update_item(_id:int,item:str,x:int,y:int,display_duration:float,layer:int,matrix:Matrix):
	return (_id,item,x,y,display_duration,layer,*matrix.to_list())
update_item=NoReturnScriptFunction("update_item",update_item)

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
	"""
	Animates the item element with the given id by calling the given function every frame with the item element as the argument.

	:param _id: ID of the item element to animate.
	:param func: Function to use to animate the item element. The function is called every frame with the item element as the argument.
	"""
	_animate_item(_id,func)

def modify_item(_id:int,func:Callable[[ItemObject],None])->None:
	"""
	Modifies the item element with the given id by calling the given function once with the item element as the argument.

	:param _id: ID of the item element to modify.
	:param func: Function to use to modify the item element. The function is called on the closest render frame.
	"""
	t=ItemObject(_id)
	if (still_exists(_id)):
		if (t.update(_id)):
			func(t)
			update_item(_id,*t.to_list())





class TextureObject(BaseObject):
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		#: Identifier of the texture. See :class:`Identifier` for more information.
		self.texture:Identifier=Identifier("none",True)

		#: X-coordinate of the texture.
		self.x:int=0

		#: Y-coordinate of the texture.
		self.y:int=0

		#: Width of the texture.
		self.width:int=16

		#: Height of the texture.
		self.height:int=16

		#: Transparency value of the texture. Use ``alpha_from_int()`` to create.
		self.alpha=1.0

		#: Time in seconds that the text will remain on screen. This property cannot be assigned, use :attr:`display_duration_modifier <TextureObject.display_duration_modifier>` to change this value.
		self.display_duration:float

		#: Modifier to the :attr:`display_duration <TextureObject.display_duration>` property.
		self.display_duration_modifier:float=0

		#: Layer of the element.
		self.layer:int=1

		#: Transformation matrix applied to the element (scale, rotation, translation).
		self.matrix:Matrix=Matrix()
		self.update(_id)

	@property
	def display_duration(self):
		return self._display_duration

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		"""
		Updates this TextureObject with the values of the texture element specified by _id.

		:param _id: ID of the texture element.
		"""
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

	def to_list(self)->list:
		"""
		Returns a list containing all the values of this TextureObject.

		:return: List containing all the values of this TextureObject.
		"""
		return [self.texture,self.x,self.y,self.width,self.height,self.alpha,self.display_duration_modifier,self.layer,self.matrix]

# noinspection PyTypeChecker
def add_texture(texture:Identifier,x:int,y:int,width:int,height:int,alpha:float,display_duration:float,layer:int=1)->int:
	"""
	Add a texture element to the screen.

	To add custom textures see :doc:`Adding Custom Textures <../custom_textures>`.

	:param texture: Identifier of the texture. See :class:`Identifier` for more information.
	:param x: X-coordinate of the texture position.
	:param y: Y-coordinate of the texture position.
	:param width: Width of the texture.
	:param height: Height of the texture
	:param alpha: Transparency value of the texture. Use ``alpha_from_int()`` to create.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:return: ID of the created element.
	"""
	return (*texture.to_list(),x,y,width,height,alpha,display_duration,layer)
add_texture=ScriptFunction("add_texture",add_texture)

# noinspection PyTypeChecker
def add_advanced_texture(texture:Identifier,x:int,y:int,width:int,height:int,alpha:float,display_duration:float,layer:int,matrix:Matrix)->int:
	"""
	Add a texture element to the screen, with additional scaling, rotation, and translation options.

	To add custom textures see :doc:`Adding Custom Textures <../custom_textures>`.

	Advanced version of :func:`add_texture` that allows custom transformations.

	:param texture: Identifier of the texture. See :class:`Identifier` for more information.
	:param x: X-coordinate of the texture position.
	:param y: Y-coordinate of the texture position.
	:param width: Width of the texture.
	:param height: Height of the texture
	:param alpha: Transparency value of the texture. Use ``alpha_from_int()`` to create.
	:param display_duration: How long the element remains on screen (in seconds).
	:param layer: Rendering layer of the element. Higher layers appear above lower ones. Default is 1.
	:param matrix: Transformation matrix applied to the element (scale, rotation, translation).
		See :class:`Matrix`.
	:return: ID of the created element.
	"""
	return (*texture.to_list(),x,y,width,height,alpha,display_duration,layer,*matrix.to_list())
add_advanced_texture=ScriptFunction("add_advanced_texture",add_advanced_texture)

# noinspection PyTypeChecker
def get_texture_object(_id:int)->dict:
	return (_id,)
get_texture_object=ScriptFunction("get_texture_object",get_texture_object)

def update_texture(_id:int,texture:Identifier,x:int,y:int,width:int,height:int,alpha:float,display_duration:float,layer:int,matrix:Matrix):
	return (_id,*texture.to_list(),x,y,width,height,alpha,display_duration,layer,*matrix.to_list())
update_texture=NoReturnScriptFunction("update_texture",update_texture)

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
	"""
	Animates the texture element with the given id by calling the given function every frame with the texture element as the argument.

	:param _id: ID of the texture element to animate.
	:param func: Function to use to animate the texture element. The function is called every frame with the texture element as the argument.
	"""
	_animate_texture(_id,func)

def modify_texture(_id:int,func:Callable[[TextureObject],None])->None:
	"""
	Modifies the texture element with the given id by calling the given function once with the texture element as the argument.

	:param _id: ID of the texture element to modify.
	:param func: Function to use to modify the texture element. The function is called on the closest render frame.
	"""
	t=TextureObject(_id)
	if (still_exists(_id)):
		if (t.update(_id)):
			func(t)
			update_texture(_id,*t.to_list())





class ShapeObject(BaseObject):
	# noinspection PyMissingConstructor
	def __init__(self,_id:int):
		self.update(_id)

	@property
	def display_duration(self):
		return self._display_duration

	# noinspection PyAttributeOutsideInit
	def update(self,_id:int):
		info=get_shape_object(_id)
		if (info is None or any(map(lambda x:x is None,info.values()))):
			return False
		self.lines=[]
		vertices=list(map(lambda v:Vertex(v["x"],v["y"],v["color"]),info["vertices"]))
		if (len(vertices)%2!=0):
			raise ValueError("Internal error, shape object has odd number of vertices!")
		for i in range(0,len(vertices),2):
			self.lines.append(Line(vertices[i],vertices[i+1]))
		self._display_duration:float=info["displayDuration"]
		self.display_duration_modifier:float=0
		self.layer:int=info["layer"]
		self.matrix=Matrix.from_dict(info)
		return True

	def to_list(self)->list:
		return [self.lines,self.display_duration_modifier,self.layer,self.matrix]

def add_shape(lines:list[Line],display_duration:float,layer:int=1)->int:
	out=[]
	for l in lines: out.extend(l.to_list())
	return (out,display_duration,layer)
add_shape=ScriptFunction("add_shape",add_shape)

def add_advanced_shape(lines:list[Line],display_duration:float,layer:int,matrix:Matrix)->int:
	out=[]
	for l in lines: out.extend(l.to_list())
	return (out,display_duration,layer,*matrix.to_list())
add_advanced_shape=ScriptFunction("add_advanced_shape",add_advanced_shape)

# noinspection PyTypeChecker
def get_shape_object(_id:int)->dict:
	return (_id,)
get_shape_object=ScriptFunction("get_shape_object",get_shape_object)

def update_shape(_id:int,lines:list[Line],display_duration:float,layer:int,matrix:Matrix):
	out=[]
	for l in lines: out.extend(l.to_list())
	return (_id,out,display_duration,layer,*matrix.to_list())
update_shape=NoReturnScriptFunction("update_shape",update_shape)

def _animate_shape(_id:int,func:Callable[[ShapeObject],None])->None:
	s=ShapeObject(_id)
	while (still_exists(_id)):
		if (s.update(_id)):
			func(s)
			l=s.to_list()
			if (any(map(lambda x:(x is None),l))): return
			update_shape(_id,*l)
			wait_next_frame()

def animate_shape(_id:int,func:Callable[[ShapeObject],None])->None:
	_animate_shape(_id,func)

def modify_shape(_id:int,func:Callable[[ShapeObject],None])->None:
	s=ShapeObject(_id)
	if (still_exists(_id)):
		if (s.update(_id)):
			func(s)
			update_shape(_id,*s.to_list())


def get_lines_for_triangle(v1:Vertex,v2:Vertex,v3:Vertex)->list[Line]:
	return [Line(v1,v2),Line(v2,v3)]

def add_triangle(p1:tuple[int,int],p2:tuple[int,int],p3:tuple[int,int],color:int,display_duration:float,layer:int=1)->int:
	return add_shape(get_lines_for_triangle(Vertex(*p1,color),Vertex(*p2,color),Vertex(*p3,color)),display_duration,layer)

def add_line(start:tuple[int,int],end:tuple[int,int],width:int,color:int,display_duration:float,layer:int=1)->int:
	start_x,start_y=start;end_x,end_y=end
	dx=end_x-start_x
	dy=end_y-start_y
	length=hypot(dx,dy)
	wx=dy/length*ceil(width/2)
	wy=-dx/length*ceil(width/2)
	v1=Vertex(start_x+wx,start_y+wy,color)
	v2=Vertex(start_x-wx,start_y-wy,color)
	v3=Vertex(end_x-wx,end_y-wy,color)
	v4=Vertex(end_x+wx,end_y+wy,color)
	return add_shape(get_lines_for_triangle(v1,v2,v3)+get_lines_for_triangle(v1,v3,v4),display_duration,layer)

def add_multiline(points:list[tuple[int,int]],width:int,color:int,display_duration:float,layer:int=1)->int:
	lines=[]
	for i in range(1,len(points)):
		start_x,start_y=points[i-1];end_x,end_y=points[i]
		dx=end_x-start_x
		dy=end_y-start_y
		length=hypot(dx,dy)
		wx=dy/length*ceil(width/2)
		wy=-dx/length*ceil(width/2)
		v1=Vertex(start_x+wx,start_y+wy,color)
		v2=Vertex(start_x-wx,start_y-wy,color)
		v3=Vertex(end_x-wx,end_y-wy,color)
		v4=Vertex(end_x+wx,end_y+wy,color)
		lines+=get_lines_for_triangle(v1,v2,v3)+get_lines_for_triangle(v1,v3,v4)
	return add_shape(lines,display_duration,layer)

def add_advanced_multiline(points:list[tuple[int,int,int]],width:int,display_duration:float,layer:int=1)->int:
	lines=[]
	for i in range(1,len(points)):
		start_x,start_y,start_color=points[i-1];end_x,end_y,end_color=points[i]
		dx=end_x-start_x
		dy=end_y-start_y
		length=hypot(dx,dy)
		wx=dy/length*ceil(width/2)
		wy=-dx/length*ceil(width/2)
		v1=Vertex(start_x+wx,start_y+wy,start_color)
		v2=Vertex(start_x-wx,start_y-wy,start_color)
		v3=Vertex(end_x-wx,end_y-wy,end_color)
		v4=Vertex(end_x+wx,end_y+wy,end_color)
		lines+=get_lines_for_triangle(v1,v2,v3)+get_lines_for_triangle(v1,v3,v4)
	return add_shape(lines,display_duration,layer)

def get_lines_for_quad(p1:tuple[int,int],p2:tuple[int,int],p3:tuple[int,int],p4:tuple[int,int],color:int)->list[Line]:
	if (len({p1,p2,p3,p4})!=4):
		raise ValueError("Points must be unique!")
	points=[p1,p2,p3,p4]
	points.sort(key=lambda p:p[1])
	tl,tr=((points[0],points[1]) if points[0][0]<points[1][0] else (points[1],points[0]))
	bl,br=((points[2],points[3]) if points[2][0]<points[3][0] else (points[3],points[2]))
	tl,tr,bl,br=Vertex(*tl,color),Vertex(*tr,color),Vertex(*bl,color),Vertex(*br,color)
	return get_lines_for_triangle(tl,bl,br)+get_lines_for_triangle(tl,br,tr)

def add_quad(p1:tuple[int,int],p2:tuple[int,int],p3:tuple[int,int],p4:tuple[int,int],color:int,display_duration:float,layer:int=1)->int:
	return add_shape(get_lines_for_quad(p1,p2,p3,p4,color),display_duration,layer)

def get_lines_for_circle(center_x:int,center_y:int,radius:int,color:int,segments:int=1000)->list[Line]:
	last=None
	first=None
	center=Vertex(center_x,center_y,color)
	lines=[]
	for i in range(segments):
		a=radians((i/segments)*360)
		x=center_x+radius*cos(a)
		y=center_y-radius*sin(a)
		v=Vertex(x,y,color)
		if (last is None):
			last=v
			first=v
		else:
			lines+=get_lines_for_triangle(v,center,last)
			last=v
	lines+=get_lines_for_triangle(first,center,last)
	return lines

def add_circle(center_x:int,center_y:int,radius:int,color:int,display_duration:float,segments:int=1000,layer=1)->int:
	return add_shape(get_lines_for_circle(center_x,center_y,radius,color,segments),display_duration,layer)

def get_lines_for_advanced_circle(center_x:int,center_y:int,radius:int,color:int,center_color:int,vertex_modifier:Callable[[Vertex,int],None],segments:int=1000)->list[Line]:
	last=None
	first=None
	center=Vertex(center_x,center_y,center_color)
	lines=[]
	for i in range(segments):
		a=radians((i/segments)*360)
		x=center_x+radius*cos(a)
		y=center_y-radius*sin(a)
		v=Vertex(x,y,color)
		vertex_modifier(v,i)
		if (last is None):
			last=v
			first=v
		else:
			lines+=get_lines_for_triangle(v,center,last)
			last=v
	lines+=get_lines_for_triangle(first,center,last)
	return lines

def add_advanced_circle(center_x:int,center_y:int,radius:int,color:int,center_color:int,vertex_modifier:Callable[[Vertex,int],None],display_duration:float,segments:int=1000,layer=1)->int:
	return add_shape(get_lines_for_advanced_circle(center_x,center_y,radius,color,center_color,vertex_modifier,segments),display_duration,layer)

def get_lines_for_ellipse(center_x:int,center_y:int,radius_x:int,radius_y:int,color:int,segments:int=1000)->list[Line]:
	last=None
	first=None
	center=Vertex(center_x,center_y,color)
	lines=[]
	for i in range(segments):
		a=radians((i/segments)*360)
		x=center_x+radius_x*cos(a)
		y=center_y-radius_y*sin(a)
		v=Vertex(x,y,color)
		if (last is None):
			last=v
			first=v
		else:
			lines+=get_lines_for_triangle(v,center,last)
			last=v
	lines+=get_lines_for_triangle(first,center,last)
	return lines

def add_ellipse(center_x:int,center_y:int,radius_x:int,radius_y:int,color:int,display_duration:float,segments:int=1000,layer=1)->int:
	return add_shape(get_lines_for_ellipse(center_x,center_y,radius_x,radius_y,color,segments),display_duration,layer)

def get_lines_for_advanced_ellipse(center_x:int,center_y:int,radius_x:int,radius_y:int,color:int,center_color:int,vertex_modifier:Callable[[Vertex,int],None],segments:int=1000)->list[Line]:
	last=None
	first=None
	center=Vertex(center_x,center_y,center_color)
	lines=[]
	for i in range(segments):
		a=radians((i/segments)*360)
		x=center_x+radius_x*cos(a)
		y=center_y-radius_y*sin(a)
		v=Vertex(x,y,color)
		vertex_modifier(v,i)
		if (last is None):
			last=v
			first=v
		else:
			lines+=get_lines_for_triangle(v,center,last)
			last=v
	lines+=get_lines_for_triangle(first,center,last)
	return lines

def add_advanced_ellipse(center_x:int,center_y:int,radius_x:int,radius_y:int,color:int,center_color:int,vertex_modifier:Callable[[Vertex,int],None],display_duration:float,segments:int=1000,layer=1)->int:
	return add_shape(get_lines_for_advanced_ellipse(center_x,center_y,radius_x,radius_y,color,center_color,vertex_modifier,segments),display_duration,layer)





class MouseObject:
	def __init__(self,mouse:dict):
		self.x=mouse["x"]
		self.y=mouse["y"]
		self.was_pressed_left=mouse["left"]
		self.was_pressed_middle=mouse["middle"]
		self.was_pressed_right=mouse["right"]

def rainbow_animation(t:BaseObject,step:int=1):
	"""
	A simple rainbow animation. Animates the ``color`` property of an element. It is recommended to set the base color to ``Colors.RED``.
	"""
	a,r,g,b=argb_to_int(t.color)
	h,s,v=rgb_to_hsv(r/255,g/255,b/255)
	h+=step/255
	if (h>255):h=0
	elif (h<0): h=255
	r,g,b=hsv_to_rgb(h,s,v)
	t.color=argb(a,int(r*255),int(g*255),int(b*255))

# noinspection PyTypeChecker
def rainbow_animation_with_speed(step:int)->Callable[[BaseObject,int],None]:
	"""
	This function returns a :func:`rainbow_animation` with the given speed.

	:param step: Amount by which to change color every frame.
	:return: :func:`rainbow_animation` with given speed.
	"""
	if (step>255 or step<-255):
		raise ValueError("Step must be between -255 and 255")
	return lambda t:rainbow_animation(t,step)

def alpha_from_int(alpha:int)->float:
	"""
	Converts an alpha value ranging from 0 to 255 (inclusive) to an integer.

	:param alpha: Alpha value to convert.
	:return: Integer representing alpha.
	"""
	if (alpha<0 or alpha>255):
		raise ValueError("Alpha must be between 0 and 255!")
	return alpha/255

def argb(a:int,r:int,g:int,b:int)->int:
	"""
	Converts a, r, g, b values into a minecraft color. Values must be between 0 and 255 (inclusive).

	:param a: Alpha value of the color.
	:param r: Red value of the color.
	:param g: Green value of the color.
	:param b: Blue value of the color.
	:return: Minecraft color with the a, r, g, b values.
	"""
	# a,r,g,b=round(a),round(r),round(g),round(b)
	if (a>255 or a<0 or r>255 or r<0 or g>255 or g<0 or b>255 or b<0):
		raise ValueError(f"a,r,g,b values must be between 0 and 255!")
	value=(a<<24)|(r<<16)|(g<<8)|b
	if (value>=0x80000000):
		value-=0x100000000
	return value

def argb_to_int(color:int)->tuple[int,int,int,int]:
	"""
	Converts an ARGB minecraft color into it's a, r, g, b values.

	:param color: Color to convert.
	:return: A, r, g, b values of the color.
	"""
	a=(color>>24)&0xFF
	r=(color>>16)&0xFF
	g=(color>>8)&0xFF
	b=color&0xFF
	return (a,r,g,b)

def remove_element(_id:int):
	"""
	Removes the element with the given id.

	:param _id: ID of the element to remove.
	"""
	return (_id,)
remove_element=NoReturnScriptFunction("remove_element",remove_element)

# noinspection PyTypeChecker
def still_exists(_id:int)->bool:
	"""
	Returns ``True`` if the element with the given id exists, ``False`` otherwise.

	:param _id: ID of the element to check.
	:return: ``True`` if the element exists, ``False`` otherwise.
	"""
	return (_id,)
still_exists=ScriptFunction("still_exists",still_exists)

def _get_mouse():
	return ()
_get_mouse=ScriptFunction("get_mouse",_get_mouse)

def get_mouse():
	return MouseObject(_get_mouse())

# noinspection PyTypeChecker
def get_font_height()->int:
	"""
	Returns the text font height.

	:return: Font height.
	"""
	return ()
get_font_height=ScriptFunction("get_font_height",get_font_height)

def clear():
	"""
	Removes all elements.
	"""
	return ()
clear=NoReturnScriptFunction("clear",clear)

def wait_next_frame():
	return ()
wait_next_frame=ScriptFunction("wait_next_frame",wait_next_frame)

def suppress_done_message():
	"""
	Removes the ``Done`` message that appears after a script finishes.
	"""
	return ()
suppress_done_message=NoReturnScriptFunction("suppress_done_message",suppress_done_message)