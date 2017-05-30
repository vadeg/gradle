/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.cpp.plugins;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.cpp.tasks.CppCompile;
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform;
import org.gradle.nativeplatform.tasks.LinkSharedLibrary;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;

import java.util.Collections;

/**
 * <p>A plugin that produces a native library from C++ source.</p>
 *
 * <p>Assumes the source files are located in `src/main/cpp`, public headers are located in `src/main/public` and implementation header files are located in `src/main/headers`.</p>
 *
 * @since 4.1
 */
@Incubating
public class CppLibraryPlugin implements Plugin<ProjectInternal> {
    @Override
    public void apply(ProjectInternal project) {
        project.getPluginManager().apply(CppBasePlugin.class);

        // TODO - extract some common code to setup the compile task and conventions
        // Add a compile task
        CppCompile compile = project.getTasks().create("compileCpp", CppCompile.class);

        compile.includes("src/main/public");
        compile.includes("src/main/headers");
        ConfigurableFileTree sourceTree = project.fileTree("src/main/cpp");
        sourceTree.include("**/*.cpp");
        sourceTree.include("**/*.c++");
        compile.source(sourceTree);
        // TODO - dependencies

        // TODO - should use PIC
        compile.setCompilerArgs(Collections.<String>emptyList());
        compile.setMacros(Collections.<String, String>emptyMap());

        // TODO - should reflect changes to build directory
        compile.setObjectFileDir(project.file("build/main/objs"));

        DefaultNativePlatform currentPlatform = new DefaultNativePlatform("current");
        compile.setTargetPlatform(currentPlatform);

        // TODO - make this lazy
        NativeToolChain toolChain = project.getModelRegistry().realize("toolChains", NativeToolChainRegistryInternal.class).getForPlatform(currentPlatform);
        compile.setToolChain(toolChain);

        // Add a link task
        final LinkSharedLibrary link = project.getTasks().create("linkMain", LinkSharedLibrary.class);
        // TODO - include only object files
        link.source(compile.getOutputs().getFiles().getAsFileTree());
        link.setLinkerArgs(Collections.<String>emptyList());
        // TODO - dependencies
        // TODO - should reflect changes to build directory
        // TODO - need to set basename and soname
        String libName = ((NativeToolChainInternal) toolChain).select(currentPlatform).getSharedLibraryName("build/lib/main");
        link.setOutputFile(project.file(libName));
        link.setTargetPlatform(currentPlatform);
        link.setToolChain(toolChain);

        project.getTasks().getByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).dependsOn(link);

        // TODO - add lifecycle tasks

        // TODO - make not resolvable
        Configuration apiElements = project.getConfigurations().create("cppApiElements");
        apiElements.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API));
        apiElements.getOutgoing().artifact(project.file("src/main/public"));

        // TODO - make not resolvable
        Configuration linkElements = project.getConfigurations().create("linkElements");
        linkElements.extendsFrom(project.getConfigurations().getByName(CppBasePlugin.IMPLEMENTATION));
        linkElements.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.NATIVE_LINK));
        // TODO - should be lazy and reflect changes to output file
        linkElements.getOutgoing().artifact(link.getOutputFile(), new Action<ConfigurablePublishArtifact>() {
            @Override
            public void execute(ConfigurablePublishArtifact artifact) {
                artifact.builtBy(link);
            }
        });
    }
}
