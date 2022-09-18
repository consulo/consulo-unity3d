/**
 * @author VISTALL
 * @since 18-Sep-22
 */
module consulo.unity3d.cg.shader
{
	requires transitive consulo.language.api;
	requires consulo.language.editor.api;
	requires consulo.language.impl;

	exports consulo.cgshader;
	exports consulo.cgshader.completion;
	exports consulo.cgshader.highlighter;
	exports consulo.cgshader.lexer;
}