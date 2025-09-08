\documentclass[10pt, letterpaper]{article}

% Russian Language Support
\usepackage{fontspec}
\usepackage{polyglossia}
\setmainlanguage{russian}
\setotherlanguage{english}
\setmainfont{Times New Roman}
\newfontfamily{\cyrillicfont}{Times New Roman}
\newfontfamily{\cyrillicfonttt}{Times New Roman}
\newfontfamily{\cyrillicfontsf}{Times New Roman}

% Essential Packages
\usepackage[margin=2cm]{geometry}
\usepackage{xcolor}
\definecolor{primaryColor}{RGB}{0, 79, 144}
\usepackage{fontawesome5}
\usepackage{hyperref}
\usepackage{array}
\usepackage{enumitem}

% Basic Setup
\pagestyle{empty}
\setlength{\parindent}{0pt}
\setlength{\parskip}{4pt}

% Custom Commands
\newcommand{\sectionrule}{\noindent\rule{\linewidth}{0.8pt}}
\newcommand{\cvsection}[1]{
    \vspace{6pt}
    {\Large\color{primaryColor}\textbf{#1}}
    \sectionrule
    \vspace{6pt}
}

\begin{document}

% Header Section
\begin{center}
   \begin{center}
    \textbf{\LARGE \textsc{${cv.title}}} \\[0.5em]  % Title in large bold small caps
    \vspace{0.2cm}
    \textbf{\Large ${cv.fullName}}                    % Name in large bold
\end{center}
    
    \begin{tabular}{@{}c@{}}
    	
        \faEnvelope\ \href{mailto:${cv.email}}{${cv.email}} \quad 
        \faPhone\ ${cv.phone} \\
        <#if cv.address??>\faMapMarker\ ${cv.address}<#else>\ </#if>
    \end{tabular}
\end{center}

% Summary Section
<#if cv.summary?? && cv.summary?has_content>
\section*{Summary}
${cv.summary}
</#if>

<#if cv.education?? && cv.education?has_content>
\section*{Education}
\begin{itemize}
<#list cv.education as edu>
    \item \textbf{${edu.institution}} (${edu.startDate} -- ${edu.endDate}) \\
    ${edu.degree} in ${edu.fieldOfStudy}
</#list>
\end{itemize}
</#if>

<#if cv.experience?? && cv.experience?has_content>
\section*{Experience}
\begin{itemize}
<#list cv.experience as exp>
    \item \textbf{${exp.company}} (${exp.startDate} -- ${exp.endDate}) \\
    \textit{${exp.position}} \\
    ${exp.description}
</#list>
\end{itemize}
</#if>

<#if cv.skills?? && cv.skills?has_content>
\section*{Skills}
\begin{itemize}
<#list cv.skills as skill>
    \item \textbf{${skill.name}} (${skill.level}/10)
</#list>
\end{itemize}
</#if>

<#if cv.languages?? && cv.languages?has_content>
\section*{Languages}
\begin{itemize}
<#list cv.languages as lang>
    \item ${lang.name} (${lang.proficiency})
</#list>
\end{itemize}
</#if>

\end{document}