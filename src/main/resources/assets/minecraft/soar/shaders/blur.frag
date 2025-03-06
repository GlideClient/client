#version 140

uniform sampler2D texture;
uniform vec2 texelSize, direction;
uniform int radius = 25;
uniform float kernels[128];

varying vec4 texCoord;

void main() {
    vec3 color = texture2D(texture, texCoord.xy).rgb * kernels[0];
    for(int i = 1; i <= radius; i++) {
        color += texture2D(texture, texCoord.xy + i * texelSize * direction).rgb * kernels[i];
        color += texture2D(texture, texCoord.xy - i * texelSize * direction).rgb * kernels[i];
    }
    gl_FragColor = vec4(color, 1.0);
}