/*
 * Copyright 2013-2021 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.unity3d.asmdef;

import consulo.json.jom.JomElement;
import consulo.json.jom.JomPropertyGetter;

import java.util.Set;

/**
 * @author VISTALL
 * @since 22/03/2021
 */
public interface AsmDefElement extends JomElement {
    @JomPropertyGetter
    String getName();

    @JomPropertyGetter
    boolean isAllowUnsafeCode();

    @JomPropertyGetter
    boolean isOverrideReferences();

    @JomPropertyGetter
    boolean isAutoReferenced();

    @JomPropertyGetter
    Set<String> getReferences();

    @JomPropertyGetter
    Set<String> getOptionalUnityReferences();

    @JomPropertyGetter
    Set<String> getIncludePlatforms();

    @JomPropertyGetter
    Set<String> getExcludePlatforms();

    @JomPropertyGetter
    Set<String> getDefineConstraints();

    @JomPropertyGetter
    Set<String> getPrecompiledReferences();
}
