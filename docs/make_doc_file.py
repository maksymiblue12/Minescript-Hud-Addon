import os.path


p="../../run/minescript/system/lib/draw_text.py"

def contains_script(x):
	return x.find("ScriptFunction")==-1

with open(p,"r") as f:
	data=f.read().split("\n")

data=list(filter(contains_script,data))

with open("../draw_text.py","w") as f:
	f.write("\n".join(data))