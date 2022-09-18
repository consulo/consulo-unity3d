/**
 * @author VISTALL
 * @since 18-Sep-22
 */
module consulo.unity3d.shaderlab
{
	requires consulo.unity3d.cg.shader;

	requires transitive consulo.language.api;
	requires consulo.language.editor.api;
	requires consulo.language.impl;
	requires consulo.csharp.psi.impl;
	requires consulo.csharp;

	exports consulo.unity3d.shaderlab.icon;
	exports consulo.unity3d.shaderlab.ide;
	exports consulo.unity3d.shaderlab.ide.completion;
	exports consulo.unity3d.shaderlab.ide.editor;
	exports consulo.unity3d.shaderlab.ide.highlight;
	exports consulo.unity3d.shaderlab.ide.refactoring;
	exports consulo.unity3d.shaderlab.lang;
	exports consulo.unity3d.shaderlab.lang.lexer;
	exports consulo.unity3d.shaderlab.lang.parser;
	exports consulo.unity3d.shaderlab.lang.parser.roles;
	exports consulo.unity3d.shaderlab.lang.psi;
	exports consulo.unity3d.shaderlab.lang.psi.impl;
	exports consulo.unity3d.shaderlab.lang.psi.light;
	exports consulo.unity3d.shaderlab.lang.psi.stub;
	exports consulo.unity3d.shaderlab.lang.psi.stub.elementType;
	exports consulo.unity3d.shaderlab.lang.psi.stub.index;
}