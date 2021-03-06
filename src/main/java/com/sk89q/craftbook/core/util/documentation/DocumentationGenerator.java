package com.sk89q.craftbook.core.util.documentation;

import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.util.ConfigValue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class DocumentationGenerator {

    public static void generateDocumentation(DocumentationProvider provider) {

        File docsFile = new File(CraftBookAPI.inst().getWorkingDirectory(), "documentation");
        docsFile.mkdir();

        File docFile = new File(docsFile, provider.getPath() + ".rst");
        docFile.getParentFile().mkdirs();

        try(PrintWriter writer = new PrintWriter(docFile)) {
            writer.print(provider.getMainDocumentation());

            //TODO output config section and perms section.
            if(provider.getConfigurationNodes().length > 0) {

                int nodeLength = 0, commentLength = 0, typeLength = 0, defaultLength = 0;

                for(ConfigValue<?> configValue : provider.getConfigurationNodes()) {
                    if(configValue.getKey().length() > nodeLength)
                        nodeLength = configValue.getKey().length();
                    if(configValue.getComment().length() > commentLength)
                        commentLength = configValue.getComment().length();
                    if(configValue.getTypeToken().getRawType().getSimpleName().length() > typeLength)
                        typeLength = configValue.getTypeToken().getRawType().getSimpleName().length();
                    if(configValue.getDefaultValue().toString().length() > defaultLength)
                        defaultLength = configValue.getDefaultValue().toString().length();
                }

                writer.println();

                String border = createStringOfLength(nodeLength, '=') + ' ' + createStringOfLength(commentLength, '=') + ' ' + createStringOfLength(typeLength, '=') + ' ' + createStringOfLength(defaultLength, '=');

                writer.println(border);
                writer.println(padToLength("Node", nodeLength+1) + padToLength("Comment", commentLength+1) + padToLength("Type", typeLength+1) + padToLength("Default", defaultLength+1));
                writer.println(border);
                for(ConfigValue<?> configValue : provider.getConfigurationNodes()) {
                    writer.println(padToLength(configValue.getKey(), nodeLength+1) + padToLength(configValue.getComment(), commentLength+1) + padToLength(configValue.getTypeToken().getRawType().getSimpleName(), typeLength+1) + padToLength(configValue.getDefaultValue().toString(), defaultLength+1));
                }
                writer.println(border);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static String createStringOfLength(int length, char character) {
        String ret = "";

        for(int i = 0; i < length; i++)
            ret += character;

        return ret;
    }

    public static String padToLength(String string, int length) {
        while(string.length() < length)
            string += ' ';
        return string;
    }
}
