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

package org.gradle.api.internal.changedetection.resources.recorders;

import com.google.common.hash.HashCode;
import org.gradle.api.internal.changedetection.resources.SnapshottableResource;
import org.gradle.api.internal.changedetection.state.NormalizedFileSnapshotCollector;
import org.gradle.api.internal.changedetection.state.TaskFilePropertyCompareStrategy;

public interface SnapshottingResultRecorder {
    void recordResult(SnapshottableResource resource, HashCode hash);
    SnapshottingResultRecorder recordCompositeResult(SnapshottableResource resource, SnapshottingResultRecorder recorder);
    HashCode getHash(NormalizedFileSnapshotCollector collector);
    TaskFilePropertyCompareStrategy getCompareStrategy();
    boolean isNormalizedPathAbsolute();
}
