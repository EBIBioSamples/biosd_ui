<%@ page import="java.net.*"%>
<%@ page import="java.util.regex.*"%>

<div id="wrapper" class="container_24">
	<header>
		<div id="global-masthead" class="masthead grid_24">
			<!--This has to be one line and no newline characters-->
			<a href="//www.ebi.ac.uk/" title="Go to the EMBL-EBI homepage"><img
				src="//www.ebi.ac.uk/web_guidelines/images/logos/EMBL-EBI/EMBL_EBI_Logo_white.png"
				alt="EMBL European Bioinformatics Institute"></a>

			<nav>
				<ul id="global-nav">
					<!-- set active class as appropriate -->
					<li class="first active" id="services"><a
						href="//www.ebi.ac.uk/services">Services</a></li>
					<li id="research"><a href="//www.ebi.ac.uk/research">Research</a></li>
					<li id="training"><a href="//www.ebi.ac.uk/training">Training</a></li>
					<li id="industry"><a href="//www.ebi.ac.uk/industry">Industry</a></li>
					<li id="about" class="last"><a href="//www.ebi.ac.uk/about">About
							us</a></li>
				</ul>
			</nav>

		</div>

		<div id="local-masthead" class="masthead grid_24 nomenu">

			<!-- local-title -->
			<!-- NB: for additional title style patterns, see http://frontier.ebi.ac.uk/web/style/patterns -->

			<div id="local-title" class="grid_12 alpha logo-title">
				<a href="${interface.application.base.path}"
					title="Back to ${interface.application.base.service.name} homepage"><img
					src="${interface.application.base.service.logo}"
					alt="${interface.application.base.service.name} logo" width="64"
					height="64"></a> <span style="padding-top: 5px"><h1>${interface.application.base.service.name}</h1></span>
			</div>
			<!-- /local-title -->

			<!-- local-search -->
			<!-- NB: if you do not have a local-search, delete the following div, and drop the class="grid_12 alpha" class from local-title above -->


<%
						String query = "";
						if (request.getParameter("keywords") != null
								&& !request.getParameter("keywords").equalsIgnoreCase("")) {
							query = request.getParameter("keywords");
						}
					
					
						String reqURI = request.getParameter("original-request-uri");
						//String x="{$interface.application.base.path}";
						String relativeReqURI = "";
						relativeReqURI = reqURI.substring(
								reqURI.indexOf("/biosamples") + 11, reqURI.length());
						String homeActive = relativeReqURI.equalsIgnoreCase("/") ? "active"
								: "";
						String groupsActive = relativeReqURI.startsWith("/browse.html")
								|| relativeReqURI.startsWith("/group") ? "active" : "";
						String samplesActive = relativeReqURI
								.startsWith("/browse_samples.html")
								|| relativeReqURI.startsWith("/sample") ? "active" : "";
						
						String submitActive = relativeReqURI.startsWith("/help/submit") ? "active"
								: "";
						String helpActive = (relativeReqURI.startsWith("/help/") && !relativeReqURI.startsWith("/help/submit")) ? "active"
								: "";
						String aboutActive = relativeReqURI.startsWith("/about.html") ? "active"
								: "";
						
						String browserPage=(groupsActive.equalsIgnoreCase("active")?"browse.html":"browse_samples.html");
						
					%>
			<div class="grid_12 omega">
				<form id="local-search" name="local-search"
					action="${interface.application.base.path}/<%=browserPage%>"
					method="get">
					
					<fieldset>

						<div class="left">
							<label> <input type="text" name="keywords"
								id="local-searchbox" value='<%=query%>'>
							</label>
							<!-- Include some example searchterms - keep them short and few! -->
							<span class="examples">Search by: <select id="biosamples_index"
								name="biosamples_index" onchange="changeSearch($('#local-search'),$('#biosamples_index option:selected').val())">
									<option value="${interface.application.base.path}/browse_samples.html">Samples</option>
									<option value="${interface.application.base.path}/browse.html"  <%=groupsActive.equalsIgnoreCase("active")?"selected":""%>>Groups</option>
							</select> &nbsp;&nbsp;Examples: <a
								href="javascript:submitFormForExamples($('#local-search'),$('#biosamples_index option:selected').val(),'leukemia')">leukemia</a>,
								<a
								href="javascript:submitFormForExamples($('#local-search'),$('#biosamples_index option:selected').val(),'ArrayExpress')">ArrayExpress</a></span>
						</div>

						<div class="right">
							<input type="submit" name="submitb" value="Search" class="submit">
							<!-- If your search is more complex than just a keyword search, you can link to an Advanced Search,
                 with whatever features you want available -->
							<%-- <span class="adv"><a
								href="${interface.application.base.path}/browse.html"
								id="adv-search" title="Advanced">Advanced</a></span> --%>
						</div>

					</fieldset>

				</form>
			</div>

			<!-- /local-search -->

			<!-- local-nav -->

			<nav>
				<ul class="grid_24" id="local-nav">
					<li class="first <%=homeActive%>"><a href="${interface.application.base.path}/"
						title="Biosamples">Home</a></li>
					<li class="<%=samplesActive%>"><a
						href="${interface.application.base.path}/browse_samples.html" title="Samples">Samples</a></li>
					<li class="<%=groupsActive%>"><a
						href="${interface.application.base.path}/browse.html" title="Groups">Sample Groups</a></li>
					<li class="<%=submitActive%>"><a
						href="${interface.application.base.path}/help/submit.html" title="Submit">Submit</a></li>
					<li class="<%=helpActive%>"><a
						href="${interface.application.base.path}/help/index.html" title="Help">Help</a></li>
					<li class="last <%=aboutActive%>"><a
						href="${interface.application.base.path}/about.html">About BioSamples</a></li>
					<!-- <li class="functional last login"><a href="#"
						class="icon icon-functional" data-icon="l">Login</a></li> -->
					<li class="functional feedback first"><a href="#"
						class="icon icon-generic" data-icon="\">Feedback</a></li>
				</ul>
			</nav>

			<!-- /local-nav -->

		</div>
	</header>

	<div id="content" role="main" class="grid_24 clearfix">
		<%
			//need thi parameter to fulfill the forms parameters
			String host = request.getScheme() + "://"
					+ request.getHeader("host");
			String contextPath = request.getContextPath();
			String originalRequestUri = request
					.getParameter("original-request-uri");
			String requestUri = request.getRequestURI();
			String pathInfo = request.getPathInfo();
			String queryString = request.getQueryString();
			String referer = request.getHeader("Referer");

			if (null != referer && referer.startsWith(host)) {
				referer = referer.replace(host, "");
			}

			String relativeUri = "";
			Pattern p = Pattern.compile("(.*?)" + contextPath + "(.*)");
			Matcher m = p.matcher(originalRequestUri);
			if (m.matches()) {
				//firstSubString = m.group(1); // may be empty
				relativeUri = m.group(2); // may be empty
			}

			String relativeReferer = "";
			if (referer != null && referer.startsWith("/")) {
				Matcher m2 = p.matcher(referer);
				if (m2.matches()) {
					relativeReferer = m2.group(2); // may be empty
				}
			}

			String secureHost = "";
			if (host.matches("^http[:]//www(dev)?[.]ebi[.]ac[.]uk$")) {
				secureHost = host.replaceAll("^http[:]//", "https://");
			}
		%>
		<section id="ae-login" style="display: none">
			<h3>
				BioSamples login<a id="ae-login-close" href="#"
					class="icon icon-functional" data-icon="x"></a>
			</h3>
			<form id="ae-login-form" method="post"
				action="<%=secureHost%><%=contextPath%>/auth">
				<fieldset>
					<label for="ae-user-field">User name</label><input
						id="ae-user-field" name="u" maxlength="50">
				</fieldset>
				<fieldset>
					<label for="ae-pass-field">Password</label><input
						id="ae-pass-field" type="password" name="p" maxlength="50">
				</fieldset>
				<span id="ae-login-remember-option"><input
					id="ae-login-remember" name="r" type="checkbox"><label
					for="ae-login-remember">Remember me</label></span><input class="submit"
					type="submit" value="Login">
				<div class="ae-login-status" style="display: none">
					<span class="alert"></span>
				</div>
				<div id="ae-login-forgot">
					<a href="#">Forgot user name or password?</a>
				</div>
			</form>
			<form id="ae-forgot-form" method="post"
				action="<%=secureHost%><%=contextPath%>/auth">
				<fieldset>
					<label for="ae-name-email-field">User name or email address</label><input
						id="ae-name-email-field" name="e" maxlength="50">
				</fieldset>
				<fieldset>
					<label for="ae-accession-field">Experiment accession
						associated with the account</label><input id="ae-accession-field" name="a"
						maxlength="14">
				</fieldset>
				<div>We will send you a reminder with your account information</div>
				<div class="ae-login-status" style="display: none">
					<span class="alert"></span>
				</div>
				<input class="submit" type="submit" value="Send">
			</form>
		</section>

		<section id="ae-feedback" style="display: none">
			<h3>
				Have your say<a id="ae-feedback-close" href="#"
					class="icon icon-functional" data-icon="x"></a>
			</h3>
			<form method="post" action="#" onsubmit="return false">
				<fieldset>
					<label for="ae-feedback-message">We value your feedback.
						Please leave your comment below.</label>
					<textarea id="ae-feedback-message" name="m"></textarea>
				</fieldset>
				<fieldset>
					<label for="ae-email-field">Optionally please enter your
						email address if you wish to get a response.<br>We will never
						share this address with anyone else.
					</label><input id="ae-email-field" name="e" maxlength="50">
				</fieldset>
				<input type="hidden" name="p"
					value="<%=host%><%=contextPath%><%=relativeUri%>?<%=queryString%>"><input
					type="hidden" name="r"
					value="<%=host%><%=contextPath%><%=relativeReferer%>"><input
					class="submit" type="submit" value="Send">
			</form>
		</section>
	<!-- </div> -->