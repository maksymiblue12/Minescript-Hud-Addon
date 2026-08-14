#version 330

#moj_import <minecraft:dynamictransforms.glsl>

layout(std140) uniform PolygonData {
	vec4 vertices[128];
	int verticesLength;
	float fade;
};

in vec4 vertexColor;
in vec2 pixelPos;
out vec4 fragColor;



// https://stackoverflow.com/questions/3838329/how-can-i-check-if-two-segments-intersect
bool ccw(vec4 a, vec4 b, vec4 c) {
    return (c.y-a.y)*(b.x-a.x)>(b.y-a.y)*(c.x-a.x);
}

bool intersects(vec4 a, vec4 b, vec4 c, vec4 d) {
    return ccw(a,c,d)!=ccw(b,c,d) && ccw(a,b,c)!=ccw(a,b,d);
}

// https://stackoverflow.com/questions/217578/how-can-i-determine-whether-a-2d-point-is-within-a-polygon
bool isInside(vec4 p) {
    vec4 lineStart=vec4(p.x,0.,0.,0.);

    int hits=0;
    for (int i=0;i<verticesLength;i++) {
        if (intersects(vertices[i],vertices[(i+1)%verticesLength],lineStart,p)) hits++;
    }

    return hits%2==1;
}

float distanceToEdge(vec4 p, vec4 a, vec4 b) {
    vec4 line=b-a;

    float t=dot(p-a,line)/dot(line.xy,line.xy);
    t=clamp(t,0.,1.);

    vec4 d=p-(a+t*line);

    return dot(d.xy,d.xy);
}

float distanceToSideSquared(vec4 p) {
    float d=-1.;

    for (int i=0;i<verticesLength;i++) {
        float edgeDistance=distanceToEdge(p,vertices[i],vertices[(i+1)%verticesLength]);
        if (d==-1.) d=edgeDistance;
        else d=min(d,edgeDistance);
    }

    return d;
}

void main() {
	float alpha=0.;

	vec4 pixelPos4=vec4(pixelPos.xy,0.,0.);

	if (isInside(pixelPos4)) alpha=1.;
	else alpha=1.-smoothstep(0.,fade*fade,distanceToSideSquared(pixelPos4));

	fragColor=vec4(vertexColor.rgb,alpha);
}