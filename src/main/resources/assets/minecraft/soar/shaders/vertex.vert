#version 120

varying vec4 texCoord;

void main() {
    texCoord = gl_MultiTexCoord0;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}