


b_name="Texture"
b_args="texture:Identifier,x:int,y:int,width:int,height:int,alpha:float"
name="update_texture"
func="updateTexture"
args="int id, String texture, boolean vanilla, int x, int y, int width, int height, double alpha, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y"



b_args2=",".join([v.split(":")[0] for v in b_args.split(",")])
func_names={"String":"getString","int":"getStrictInt","double":"getDouble","boolean":"getBoolean"}
type_names={"String":"str","int":"int","double":"float","boolean":"bool"}
args1=[f"args.{func_names[v.strip().split(' ')[0]]}({i})" for i,v in enumerate(args.split(","))]
args2=[f"{v.strip().split(' ')[1]}:{type_names[v.strip().split(' ')[0]]}" for v in args.split(",")]
args3=[v.strip().split(' ')[1] for v in args.split(",")]

tab="	"
out=f"{tab*3}case \"{name}\" -> {{\n{tab*4}args.expectSize({len(args1)});\n{tab*4}DrawHelper.getInstance().{func}({', '.join(args1)});\n{tab*4}cir.setReturnValue(ScriptValue.of());\n{tab*4}cir.cancel();\n{tab*3}}}"
print(out)

out2=f"def {name}({','.join(args2)}):\n{tab}return ({','.join(args3)})\n{name}=ScriptFunction(\"{name}\",{name})"
print()
print(out2)

out3=f"""
class {b_name}Object(BaseObject):
    # noinspection PyMissingConstructor
    def __init__(self,_id:int):
        self.update(_id)

    # noinspection PyAttributeOutsideInit
    def update(self,_id:int):
        info=get_{b_name.lower()}_object(_id)
        if (info is None):
            return False
        self.{b_name.lower()}:str=info["{b_name.lower()}"]
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
        return self.{b_name.lower()},self.x,self.y,self.display_duration_modifier,self.layer,self.matrix

# noinspection PyTypeChecker
def add_{b_name.lower()}({b_args},display_duration:float,layer:int)->int:
    return ({b_args2},display_duration,layer)
add_{b_name.lower()}=ScriptFunction("add_{b_name.lower()}",add_{b_name.lower()})

# noinspection PyTypeChecker
def add_advanced_{b_name.lower()}({b_args},display_duration:float,layer:int,matrix:Matrix)->int:
    return ({b_args2},display_duration,layer,*matrix.to_list())
add_advanced_{b_name.lower()}=ScriptFunction("add_advanced_{b_name.lower()}",add_advanced_{b_name.lower()})

# noinspection PyTypeChecker
def get_{b_name.lower()}_object(_id:int)->dict:
    return (_id,)
get_{b_name.lower()}_object=ScriptFunction("get_{b_name.lower()}_object",get_{b_name.lower()}_object)

def update_{b_name.lower()}(_id:int,{b_args},display_duration:float,layer:int,matrix:Matrix):
    return (_id,{b_args2},display_duration,layer,*matrix.to_list())
update_{b_name.lower()}=ScriptFunction("update_{b_name.lower()}",update_{b_name.lower()})

def _animate_{b_name.lower()}(_id:int,func:Callable[[{b_name}Object],None])->None:
    t={b_name}Object(_id)
    while (still_exists(_id)):
        if (t.update(_id)):
            func(t)
            l=t.to_list()
            if (any(map(lambda x:x is None,l))): return
            update_{b_name.lower()}(_id,*l)
            wait_next_frame()

def animate_{b_name.lower()}(_id:int,func:Callable[[{b_name}Object],None])->None:
    _animate_{b_name.lower()}(_id,func)

def animate_{b_name.lower()}_on_different_thread(_id:int,func:Callable[[{b_name}Object],None])->None:
    task=Thread(target=_animate_{b_name.lower()},args=(_id,func))
    task.start()
    task.join()

def modify_{b_name.lower()}(_id:int,func:Callable[[{b_name}Object],None])->None:
    t={b_name}Object(_id)
    if (still_exists(_id)):
        if (t.update(_id)):
            func(t)
            update_{b_name.lower()}(_id,*t.to_list())
"""
print()
print(out3)
