/*
 * Copyright 2013-2016 consulo.io
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

package consulo.unity3d.editor;

import java.io.IOException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.gson.Gson;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.ProcessInfo;
import consulo.unity3d.run.debugger.UnityProcess;
import consulo.unity3d.run.debugger.UnityProcessDialog;

/**
 * @author VISTALL
 * @since 17.01.2016
 */
public class UnityEditorCommunication
{
	private static final Logger LOGGER = Logger.getInstance(UnityEditorCommunication.class);

	@Nullable
	public static UnityProcess findEditorProcess()
	{
		JavaSysMon javaSysMon = new JavaSysMon();
		ProcessInfo[] processInfos = javaSysMon.processTable();

		UnityProcess unityProcess = null;
		for(ProcessInfo processInfo : processInfos)
		{
			String name = processInfo.getName();
			if(name.equalsIgnoreCase("unity.exe") || name.equalsIgnoreCase("unity") || name.equalsIgnoreCase("unity.app"))
			{
				unityProcess = new UnityProcess(processInfo.getPid(), processInfo.getName(), "localhost", UnityProcessDialog.buildDebuggerPort(processInfo.getPid()));
				break;
			}
		}

		return unityProcess;
	}

	public static boolean request(@NotNull Project project, @NotNull Object postObject, boolean silent)
	{
		UnityProcess editorProcess = findEditorProcess();

		if(editorProcess == null)
		{
			if(!silent)
			{
				Messages.showErrorDialog(project, "UnityEditor is not opened", "Consulo");
			}
			return false;
		}

		int port = editorProcess.getPort() + 2000;

		Gson gson = new Gson();
		String urlPart = postObject.getClass().getSimpleName();
		HttpPost post = new HttpPost("http://localhost:" + port + "/" + StringUtil.decapitalize(urlPart));
		post.setEntity(new StringEntity(gson.toJson(postObject), CharsetToolkit.UTF8_CHARSET));
		post.setHeader("Content-Type", "application/json");

		CloseableHttpClient client = null;
		try
		{
			int timeOut = 5 * 1000;
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeOut).setConnectTimeout(timeOut).setSocketTimeout(timeOut).build();
			client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
			String data = client.execute(post, httpResponse -> EntityUtils.toString(httpResponse.getEntity(), CharsetToolkit.UTF8_CHARSET));

			UnityEditorResponse unityEditorResponse = gson.fromJson(data, UnityEditorResponse.class);
			if(!unityEditorResponse.success)
			{
				if(!silent)
				{
					Messages.showInfoMessage(project, "Unity cant execute this request", "Consulo");
				}
			}
			return unityEditorResponse.success;
		}
		catch(IOException e)
		{
			LOGGER.warn(e);

			if(!silent)
			{
				Messages.showErrorDialog(project, "UnityEditor is not opened", "Consulo");
			}
		}
		finally
		{
			if(client != null)
			{
				try
				{
					client.close();
				}
				catch(IOException e)
				{
					//
				}
			}
		}
		return false;
	}
}
