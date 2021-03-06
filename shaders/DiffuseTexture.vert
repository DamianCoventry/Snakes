#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoordinate;

out vec2 outTexCoordinate;
out vec4 outDiffuseColour;

uniform vec4 diffuseColour;
uniform mat4 mvpMatrix;

void main()
{
    gl_Position = mvpMatrix * vec4(position, 1.0);
    outTexCoordinate = texCoordinate;
    outDiffuseColour = diffuseColour;
}
