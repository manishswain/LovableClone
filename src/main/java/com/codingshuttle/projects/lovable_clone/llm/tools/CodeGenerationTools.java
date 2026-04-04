package com.codingshuttle.projects.lovable_clone.llm.tools;

import com.codingshuttle.projects.lovable_clone.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CodeGenerationTools {

    private final ProjectFileService projectFileService;
    private final Long projectId;

    @Tool(name = "read_files",
            description = "Read the content of files. Only input the file names present inside the FILE_TREE. DO NOT input any path which is not present under the FILE_TREE.")
    public List<String> readFiles(@ToolParam(description = "List of relative paths (e.g., ['src/App.tsx'])")
                                      List<String> paths){

        List<String> result = new ArrayList<String>();

        for(String path : paths){
            String cleanPath = path.startsWith("/") ? path.substring(1) : path;

            String content = projectFileService.getFileContent(projectId, cleanPath).content();

            result.add(String.format(
                    "--- START OF THE FILE: %s ---\n%s\n--- END OF THE FILE ---"
                    , cleanPath, content));
        }

        return result;
    }
}
