//
// Snake Game
// https://en.wikipedia.org/wiki/Snake_(video_game_genre)
//
// Based on the 1976 arcade game Blockade, and the 1991 game Nibbles
// https://en.wikipedia.org/wiki/Blockade_(video_game)
// https://en.wikipedia.org/wiki/Nibbles_(video_game)
//
// This implementation is Copyright (c) 2021, Damian Coventry
// All rights reserved
// Written for Massey University course 159.261 Game Programming (Assignment 1)
//

package com.snakegame.client;

import java.io.*;
import java.util.ArrayList;

/**
 * This class opens and parses a Wavefront .mtl file. The data from the file are saved into private members of this
 * class. The users of this class can use the public accessors to read the data.
 * */

// https://en.wikipedia.org/wiki/Wavefront_.obj_file#Material_template_library
public class MtlFile {
    private final ArrayList<Material> m_Materials;

    public static class Material {
        private final String m_Name;
        private Colour m_AmbientColour;
        private Colour m_DiffuseColour;
        private Colour m_SpecularColour;
        private Colour m_EmissiveColour;
        private float m_SpecularExponent;
        private float m_IndexOfRefraction;
        private float m_Dissolved;
        private float m_Transparency;
        private Colour m_TransmissionFilterColour;
        private int m_IlluminationModel;
        private String m_AmbientTexture;
        private String m_DiffuseTexture;
        private String m_SpecularTexture;
        private String m_EmissiveTexture;
        private String m_SpecularExponentTexture;
        private String m_IndexOfRefractionTexture;
        private String m_DissolvedTexture;
        private String m_TransparencyTexture;
        private String m_TransmissionFilterTexture;

        public Material(String name) {
            m_Name = name;
        }

        public String getName() {
            return m_Name;
        }

        public void setAmbientColour(Colour ambientColour) {
            m_AmbientColour = ambientColour;
        }
        Colour getAmbientColour() {
            return m_AmbientColour;
        }
        public void setDiffuseColour(Colour diffuseColour) {
            m_DiffuseColour = diffuseColour;
        }
        Colour getDiffuseColour() {
            return m_DiffuseColour;
        }
        public void setSpecularColour(Colour specularColour) {
            m_SpecularColour = specularColour;
        }
        Colour getSpecularColour() {
            return m_SpecularColour;
        }
        public void setEmissiveColour(Colour emissiveColour) {
            m_EmissiveColour = emissiveColour;
        }
        Colour getEmissiveColour() {
            return m_EmissiveColour;
        }
        public void setSpecularExponent(float specularExponent) {
            m_SpecularExponent = specularExponent;
        }
        float getSpecularExponent() {
            return m_SpecularExponent;
        }
        public void setIndexOfRefraction(float indexOfRefraction) {
            m_IndexOfRefraction = indexOfRefraction;
        }
        float getIndexOfRefraction() {
            return m_IndexOfRefraction;
        }
        public void setDissolved(float Dissolved) {
            m_Dissolved = Dissolved;
        }
        float getDissolved() {
            return m_Dissolved;
        }
        public void setTransparency(float Transparency) {
            m_Transparency = Transparency;
        }
        float getTransparency() {
            return m_Transparency;
        }
        public void setTransmissionFilterColour(Colour transmissionFilterColour) {
            m_TransmissionFilterColour = transmissionFilterColour;
        }
        Colour getTransmissionFilterColour() {
            return m_TransmissionFilterColour;
        }
        public void setIlluminationModel(int illuminationModel) {
            m_IlluminationModel = illuminationModel;
        }
        int getIlluminationModel() {
            return m_IlluminationModel;
        }
        public void setAmbientTexture(String ambientTexture) {
            m_AmbientTexture = ambientTexture;
        }
        String getAmbientTexture() {
            return m_AmbientTexture;
        }
        public void setDiffuseTexture(String diffuseTexture) {
            m_DiffuseTexture = diffuseTexture;
        }
        String getDiffuseTexture() {
            return m_DiffuseTexture;
        }
        public void setSpecularTexture(String specularTexture) {
            m_SpecularTexture = specularTexture;
        }
        String getSpecularTexture() {
            return m_SpecularTexture;
        }
        public void setEmissiveTexture(String emissiveTexture) {
            m_EmissiveTexture = emissiveTexture;
        }
        String getEmissiveTexture() {
            return m_EmissiveTexture;
        }
        public void setSpecularExponentTexture(String specularExponentTexture) {
            m_SpecularExponentTexture = specularExponentTexture;
        }
        String getSpecularExponentTexture() {
            return m_SpecularExponentTexture;
        }
        public void setIndexOfRefractionTexture(String indexOfRefractionTexture) {
            m_IndexOfRefractionTexture = indexOfRefractionTexture;
        }
        String getIndexOfRefractionTexture() {
            return m_IndexOfRefractionTexture;
        }
        public void setDissolvedTexture(String DissolvedTexture) {
            m_DissolvedTexture = DissolvedTexture;
        }
        String getDissolvedTexture() {
            return m_DissolvedTexture;
        }
        public void setTransparencyTexture(String TransparencyTexture) {
            m_TransparencyTexture = TransparencyTexture;
        }
        String getTransparencyTexture() {
            return m_TransparencyTexture;
        }
        public void setTransmissionFilterTexture(String transmissionFilterTexture) {
            m_TransmissionFilterTexture = transmissionFilterTexture;
        }
        String getTransmissionFilterTexture() {
            return m_TransmissionFilterTexture;
        }
    }

    public static class Colour {
        public float m_R, m_G, m_B;
        public Colour(float r, float g, float b) {
            m_R = r;
            m_G = g;
            m_B = b;
        }
    }

    public MtlFile(String fileName) throws Exception {
        m_Materials = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
	        File file = new File(fileName);
	        bufferedReader = new BufferedReader(new FileReader(file));
	        String line = bufferedReader.readLine();
	        while (line != null) {
	            line = line.trim();
	            if (!line.isEmpty() && !line.startsWith("#")) {
	                parseLine(line);
	            }
	            line = bufferedReader.readLine();
	        }
		}
        finally {
        	if (bufferedReader != null) {
        		bufferedReader.close();
        	}
        }
    }

    public ArrayList<Material> getMaterials() {
        return m_Materials;
    }

    private void parseLine(String line) {
        String[] words = line.split(" ");
        if (words[0].equals("newmtl")) {
            parseNewMaterial(words);
        }
        else if (words[0].equals("Ka")) { // ambient colour
            parseAmbientColour(words);
        }
        else if (words[0].equals("Kd")) { // diffuse colour
            parseDiffuseColour(words);
        }
        else if (words[0].equals("Ks")) { // specular colour
            parseSpecularColour(words);
        }
        else if (words[0].equals("Ke")) { // emissive colour
            parseEmissiveColour(words);
        }
        else if (words[0].equals("Ns")) { // specular exponent
            parseSpecularExponent(words);
        }
        else if (words[0].equals("Ni")) { // index of refraction
            parseOfRefraction(words);
        }
        else if (words[0].equals("d")) { // Dissolved
            parseDissolved(words);
        }
        else if (words[0].equals("Tr")) { // Transparency
            parseTransparency(words);
        }
        else if (words[0].equals("Tf")) { // transmission filter colour
            parseTransmissionFilterColour(words);
        }
        else if (words[0].equals("illum")) { // illumination model
            parseIlluminationModel(words);
        }
        else if (words[0].equals("map_Ka")) { // ambient texture
            parseAmbientTexture(words);
        }
        else if (words[0].equals("map_Kd")) { // diffuse texture
            parseDiffuseTexture(words);
        }
        else if (words[0].equals("map_Ks")) { // specular texture
            parseSpecularTexture(words);
        }
        else if (words[0].equals("map_Ke")) { // emissive texture
            parseEmissiveTexture(words);
        }
        else if (words[0].equals("map_Ns")) { // specular exponent texture
            parseSpecularExponentTexture(words);
        }
        else if (words[0].equals("map_Ni")) { // index of refraction texture
            parseIndexOfRefractionTexture(words);
        }
        else if (words[0].equals("map_d")) { // Dissolved texture
            parseDissolvedTexture(words);
        }
        else if (words[0].equals("map_Tr")) { // Transparency texture
            parseTransparencyTexture(words);
        }
        else if (words[0].equals("map_Tf")) { // transmission filter texture
            parseTransmissionFilterTexture(words);
        }
    }

    private void parseNewMaterial(String[] words) {
        if (words.length == 2) {
            m_Materials.add(new Material(words[1]));
        }
    }

    private void parseAmbientColour(String[] words) {
        if (words.length == 4 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setAmbientColour(parseColour(words));
        }
    }

    private void parseDiffuseColour(String[] words) {
        if (words.length == 4 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setDiffuseColour(parseColour(words));
        }
    }

    private void parseSpecularColour(String[] words) {
        if (words.length == 4 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setSpecularColour(parseColour(words));
        }
    }

    private void parseEmissiveColour(String[] words) {
        if (words.length == 4 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setEmissiveColour(parseColour(words));
        }
    }

    private void parseSpecularExponent(String[] words) {
        // Blender stores its 'Roughness' value from its Principled BSDF within the Ns value.
        // It maps (0, 1) from the Blender GUI to (900, 0) for some weird reason.
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setSpecularExponent(Float.parseFloat(words[1]));
        }
    }

    private void parseOfRefraction(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setIndexOfRefraction(Float.parseFloat(words[1]));
        }
    }

    private void parseDissolved(String[] words) {
        // 0 == fully transparent, 1 = fully opaque
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setDissolved(Float.parseFloat(words[1]));
        }
    }

    private void parseTransparency(String[] words) {
        // 0 == fully opaque, 1 = fully transparent
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setTransparency(Float.parseFloat(words[1]));
        }
    }

    private void parseTransmissionFilterColour(String[] words) {
        if (words.length == 4 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setTransmissionFilterColour(parseColour(words));
        }
    }

    private void parseIlluminationModel(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setIlluminationModel(Integer.parseInt(words[1]));
        }
    }

    private void parseAmbientTexture(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setAmbientTexture(words[1]);
        }
    }

    private void parseDiffuseTexture(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setDiffuseTexture(words[1]);
        }
    }

    private void parseSpecularTexture(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setSpecularTexture(words[1]);
        }
    }

    private void parseEmissiveTexture(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setEmissiveTexture(words[1]);
        }
    }

    private void parseSpecularExponentTexture(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setSpecularExponentTexture(words[1]);
        }
    }

    private void parseIndexOfRefractionTexture(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setIndexOfRefractionTexture(words[1]);
        }
    }

    private void parseDissolvedTexture(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setDissolvedTexture(words[1]);
        }
    }

    private void parseTransparencyTexture(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setTransparencyTexture(words[1]);
        }
    }

    private void parseTransmissionFilterTexture(String[] words) {
        if (words.length == 2 && !m_Materials.isEmpty()) {
            m_Materials.get(m_Materials.size() - 1).setTransmissionFilterTexture(words[1]);
        }
    }

    private Colour parseColour(String[] words) {
        if (words.length == 4) {
            return new Colour(
                    Float.parseFloat(words[1]),
                    Float.parseFloat(words[2]),
                    Float.parseFloat(words[3])
            );
        }
        return null;
    }
}
