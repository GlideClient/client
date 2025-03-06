#version 140

uniform sampler2D texture;
uniform vec2 texelSize, direction;
uniform int radius = 25;
uniform float kernels[128];
uniform bool ignoreAlpha = false;
varying vec4 texCoord;

void main() {
    vec4 color = vec4(0.0);
    float weightSum = kernels[0];

    color += texture2D(texture, texCoord.xy) * kernels[0];
    for (int i = 1; i <= radius; i++) {
        vec2 offset = i * texelSize * direction;
        vec2 coordPlus = clamp(texCoord.xy + offset, vec2(0.0), vec2(1.0));
        vec2 coordMinus = clamp(texCoord.xy - offset, vec2(0.0), vec2(1.0));
        float weight = kernels[i];

        color += texture2D(texture, coordPlus) * weight;
        color += texture2D(texture, coordMinus) * weight;
        weightSum += 2.0 * weight;
    }
    color /= weightSum;

    if (ignoreAlpha) color.a = 1.0;
    gl_FragColor = color;
}