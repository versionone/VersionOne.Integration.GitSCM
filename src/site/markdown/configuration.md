## Configuration

### Configure the Integration

Open the file configuration.xml in a text editor (i.e. notepad.exe), change the configuration parameters listed below, and save your changes.

The `VersionOneConnection` element specifies how the integration connects to your VersionOne server.

<table>
	<tbody>
		<tr>
			<th>Element</th>
			<th>Description</th>
			<th>Example</th>
		</tr>
		<tr>
			<td>Path</td>
			<td>URL to VersionOne Server</td>
			<td>https://www14.v1host.com/v1sdktesting/</td>
		</tr>
		<tr>
			<td>UserName</td>
			<td>Valid VersionOne user.</td>
			<td>admin</td>
		</tr>
		<tr>
			<td>Password</td>
			<td>Password for user</td>
			<td>admin</td>
		</tr>
	</tbody>
</table>

If your VersionOne instance is configured for Windows Integrated Authentication, you need to leave the `UserName` and `Password` elements blank.

The nested `ProxySettings` element specifies proxy settings for	your VersionOne connection.

<table>
	<tbody>
		<tr>
			<th>Element</th>
			<th>Description</th>
			<th>Example</th>
		</tr>
		<tr>
			<td>UseProxy</td>
			<td>Indicates if the connection uses a proxy</td>
			<td>false</td>
		</tr>
		<tr>
			<td>Path</td>
			<td>URL of Proxy Server</td>
			<td>http://proxyserver:3128</td>
		</tr>
		<tr>
			<td>UserName</td>
			<td>Username for proxy</td>
			<td>proxyUser</td>
		</tr>
		<tr>
			<td>Password</td>
			<td>Password for Proxy user</td>
			<td>proxyUserPass</td>
		</tr>
	</tbody>
</table>

The `GitSettings` element contains `Instance` elements. Each Instance element specifies how the integration connects to a Git repository. The example below only shows a single repository. To connect with multiple repositories, simply add more Instance elements.

<table>
	<tbody>
		<tr>
			<th>Element</th>
			<th>Description</th>
			<th>Example</th>
		</tr>
		<tr>
			<td>Path</td>
			<td>Path to Repository</td>
			<td>git@github.com:account/repo1.git</td>
		</tr>
		<tr>
			<td>Password</td>
			<td>Password for repository</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>SshPassphrase</td>
			<td>SSH passphrase for accessing the Git</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>WatchedBranchName</td>
			<td>Name of branch to watch</td>
			<td>master</td>
		</tr>
		<tr>
			<td>UseBranchName</td>
			<td>Determines how the integration will parse VersionOne identifiers. The default setting (false) configures the integration to parse the commit comment to find a VersionOne ID. Another pattern for agile version control is "branch on story". For this pattern, set UseBranchName to true to enable the integration to parse the branch name for a VersionOne ID.</td>
			<td>false</td>
		</tr>
	</tbody>
</table>

Each `Instance` element has one `Link` element that specifies how Links are created in VersionOne.

<table>
	<tbody>
		<tr>
			<th>Element</th>
			<th>Description</th>
			<th>Example</th>
		</tr>
		<tr>
			<td>Name</td>
			<td>Determines the Title on the Link in VersionOne. If you'd
				like to include the Git commit identifier in the title, add {0}</td>
			<td>ChangeSet: {0}</td>
		</tr>
		<tr>
			<td>URL</td>
			<td>The contents of this element are used to create a URL to
				the actual Git commit. The {0} is replaced with the Git commit
				identifier</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>OnMenu</td>
			<td>Determines if the Link Title appears on the Story or
				Defect Details page in VersionOne.</td>
			<td>true</td>
		</tr>
	</tbody>
</table>

The following elements control integration behavior.

<table>
	<tbody>
		<tr>
			<th>Element</th>
			<th>Description</th>
			<th>Example</th>
		</tr>
		<tr>
			<td>LocalDirectory</td>
			<td>Name of a local directory to use. The integration uses <a href='http://docs.oracle.com/javase/1.4.2/docs/api/java/io/File.html'>Java conventions for the path separator</a>.</td>
			<td>c:\\temp\\git\\data\\</td>
		</tr>
		<tr>
			<td>Timeout</td>
			<td>Determines how long the integration waits before polling
				Git. The value is in milliseconds.</td>
			<td>60000 = 1 minute</td>
		</tr>
		<tr>
			<td>AlwaysCreate</td>
			<td>Determines when the integration creates a VersionOne
				ChangeSet.</td>
			<td>false</td>
		</tr>
		<tr>
			<td>ChangeComment</td>
			<td>The text used when creating or updating a VersionOne
				ChangeSet.</td>
			<td>Updated by Git</td>
		</tr>
		<tr>
			<td>ReferenceAttribute</td>
			<td>Determines which VersionOne attribute is considered when
				looking for Stories or Defects.</td>
			<td>Number</td>
		</tr>
		<tr>
			<td>ReferenceExpression</td>
			<td>Determines the Regular Expression used when searching Git
				comments for VersionOne Story or Defect Identifiers</td>
			<td>[[A-Z]{1,2}-[0-9]+]]</td>
		</tr>
	</tbody>
</table>

### Example Configuration File

<div class="hlcode">
<div class="syntax"><pre><span class="cp">&lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot; ?&gt;</span>
<span class="nt">&lt;Configuration&gt;</span>

    <span class="nt">&lt;LocalDirectory&gt;</span>./v1sdktesting<span class="nt">&lt;/LocalDirectory&gt;</span>
    <span class="nt">&lt;PollIntervalInSeconds&gt;</span>10<span class="nt">&lt;/PollIntervalInSeconds&gt;</span> 

    <span class="nt">&lt;VersionOneConnection&gt;</span>
        <span class="nt">&lt;Path&gt;</span>https://www14.v1host.com/v1sdktesting/<span class="nt">&lt;/Path&gt;</span>
        <span class="nt">&lt;UserName&gt;</span>admin<span class="nt">&lt;/UserName&gt;</span>
        <span class="nt">&lt;Password&gt;</span>admin<span class="nt">&lt;/Password&gt;</span>
        <span class="c">&lt;!-- true - not use UserName and Password data in authentication--&gt;</span>
        <span class="nt">&lt;IntegratedAuth&gt;</span>false<span class="nt">&lt;/IntegratedAuth&gt;</span>
        <span class="nt">&lt;ProxySettings&gt;</span>
            <span class="nt">&lt;UseProxy&gt;</span>false<span class="nt">&lt;/UseProxy&gt;</span>
            <span class="nt">&lt;Path&gt;</span>http://proxyserver:3128/<span class="nt">&lt;/Path&gt;</span>
            <span class="nt">&lt;UserName&gt;</span>proxyUser<span class="nt">&lt;/UserName&gt;</span>
            <span class="nt">&lt;Password&gt;</span>proxyUserPass<span class="nt">&lt;/Password&gt;</span>
        <span class="nt">&lt;/ProxySettings&gt;</span>
    <span class="nt">&lt;/VersionOneConnection&gt;</span>

   <span class="nt">&lt;GitConnections&gt;</span>
        <span class="nt">&lt;GitConnection&gt;</span>
            <span class="nt">&lt;Path&gt;</span>git://github.com/account/v1sdktesting.git<span class="nt">&lt;/Path&gt;</span>
            <span class="nt">&lt;Password&gt;</span>pass<span class="nt">&lt;/Password&gt;</span>
            <span class="nt">&lt;SshPassphrase&gt;&lt;/SshPassphrase&gt;</span>
            <span class="nt">&lt;WatchedBranchName&gt;&lt;/WatchedBranchName&gt;</span>
            <span class="nt">&lt;UseBranchName&gt;</span>false<span class="nt">&lt;/UseBranchName&gt;</span>
            <span class="nt">&lt;Link&gt;</span>
                <span class="nt">&lt;NameTemplate&gt;</span>ChangeSet: {0}<span class="nt">&lt;/NameTemplate&gt;</span>
                <span class="nt">&lt;UrlTemplate&gt;</span>https://github.com/account/v1sdktesting/commit/{0}<span class="nt">&lt;/UrlTemplate&gt;</span>
                <span class="nt">&lt;OnMenu&gt;</span>true<span class="nt">&lt;/OnMenu&gt;</span>
            <span class="nt">&lt;/Link&gt;</span>
       <span class="nt">&lt;/GitConnection&gt;</span>
    <span class="nt">&lt;/GitConnections&gt;</span>

    <span class="nt">&lt;Timeout&gt;</span>10000<span class="nt">&lt;/Timeout&gt;</span>
 <span class="nt">&lt;ChangeSet&gt;</span>   
        <span class="nt">&lt;AlwaysCreate&gt;</span>false<span class="nt">&lt;/AlwaysCreate&gt;</span>
        <span class="nt">&lt;NameTemplate&gt;</span>{0}<span class="nt">&lt;/NameTemplate&gt;</span>
        <span class="nt">&lt;NameTemplateDateFormat&gt;</span>yyyy-MM-dd HH:mm:ss<span class="nt">&lt;/NameTemplateDateFormat&gt;</span>
        <span class="nt">&lt;NameTemplateRepositoryFormat&gt;</span>NameOnly<span class="nt">&lt;/NameTemplateRepositoryFormat&gt;</span>
        <span class="nt">&lt;ChangeComment&gt;</span>Updated by Git<span class="nt">&lt;/ChangeComment&gt;</span>
        <span class="nt">&lt;ReferenceAttribute&gt;</span>Number<span class="nt">&lt;/ReferenceAttribute&gt;</span>
        <span class="c">&lt;!--</span>
<span class="c">            === Sample Regexes ===</span>
<span class="c">            To Match S-01001 (Matches &quot;S-01001&quot;):                   [A-Z]{1,2}-[0-9]+</span>
<span class="c">            To match #Reference (matches only &quot;Reference&quot;):         (?&lt;=#)[a-zA-Z]+\b</span>
<span class="c">            To match &quot;V1:Reference&quot;  (matches only &quot;Reference&quot;):    (?&lt;=V1:)[a-zA-Z]+\b</span>
<span class="c">        --&gt;</span>
        <span class="nt">&lt;ReferenceExpression&gt;</span><span class="cp">&lt;![CDATA[[A-Z]{1,2}-[0-9]+]]&gt;</span><span class="nt">&lt;/ReferenceExpression&gt;</span>
<span class="nt">&lt;/ChangeSet&gt;</span>

<span class="nt">&lt;/Configuration&gt;</span>
</pre></div>

</div>

