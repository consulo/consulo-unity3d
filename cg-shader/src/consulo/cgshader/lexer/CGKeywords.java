/*
 * Copyright 2013-2015 must-be.org
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

package consulo.cgshader.lexer;

import java.util.Set;

import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public interface CGKeywords
{
	Set<String> KEYWORDS = ContainerUtil.newHashSet("asm", "asm_fragment", "auto", "case", "class", "column_major", "compile", "const", "const_cast", "continue", "decl", "default", "delete",
			"discard", "do", "dword", "dynamic_cast", "else", "emit", "enum", "explicit", "extern", "for", "friend", "get", "if", "inline", "interface", "matrix", "mutable", "new", "operator",
			"packed", "pass", "pixelfragment", "pixelshader", "private", "protected", "public", "register", "reinterpret_cast", "row_major", "shared", "sizeof", "static_cast", "string", "struct",
			"switch", "technique", "template", "texture", "texture1D", "texture2D", "texture3D", "textureCUBE", "textureRECT", "this", "typedef", "typeid", "typename", "union", "vector",
			"vertexfragment", "vertexshader", "virtual", "volatile", "while", "break", "goto", "return", "true", "false", "NULL", "const", "extern", "in", "inline", "inout", "static", "out",
			"uniform", "varying", "float", "float1", "float2", "float3", "float4", "float1x1", "float1x2", "float1x3", "float1x4", "float2x1", "float2x2", "float2x3", "float2x4", "float3x1",
			"float3x2", "float3x3", "float3x4", "float4x1", "float4x2", "float4x3", "float4x4", "half", "half1", "half2", "half3", "half4", "half1x1", "half1x2", "half1x3", "half1x4", "half2x1",
			"half2x2", "half2x3", "half2x4", "half3x1", "half3x2", "half3x3", "half3x4", "half4x1", "half4x2", "half4x3", "half4x4", "int", "int1", "int2", "int3", "int4", "int1x1", "int1x2",
			"int1x3", "int1x4", "int2x1", "int2x2", "int2x3", "int2x4", "int3x1", "int3x2", "int3x3", "int3x4", "int4x1", "int4x2", "int4x3", "int4x4", "fixed", "fixed1", "fixed2", "fixed3",
			"fixed4", "fixed1x1", "fixed1x2", "fixed1x3", "fixed1x4", "fixed2x1", "fixed2x2", "fixed2x3", "fixed2x4", "fixed3x1", "fixed3x2", "fixed3x3", "fixed3x4", "fixed4x1", "fixed4x2",
			"fixed4x3", "fixed4x4", "bool", "bool1", "bool2", "bool3", "bool4", "bool1x1", "bool1x2", "bool1x3", "bool1x4", "bool2x1", "bool2x2", "bool2x3", "bool2x4", "bool3x1", "bool3x2",
			"bool3x3", "bool3x4", "bool4x1", "bool4x2", "bool4x3", "bool4x4", "sampler", "sampler1D", "sampler1DARRAY", "sampler2D", "sampler2DARRAY", "sampler3D", "samplerRECT", "samplerCUBE",
			"unsigned", "signed", "char", "short", "long", "double", "cint", "cfloat", "void", "enum", "struct", "class", "union", "typedef", "packed");
}
