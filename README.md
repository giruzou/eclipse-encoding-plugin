# Encoding StatusBar Eclipse Plugin
Show file information for the active editor in the Eclipse status bar.  

**Show file encoding and line ending**  
Line ending support showing CRLF, CR, LF and Mixed, can also be changed.  
![](image/ending_select.jpg)  

**Change "Text File Encoding" setting by file property**  
To add the encoding, use Preference > General > Workspace > Text File Encoding > Other.  
![](image/encoding_select.jpg)  
Inheritance: Inherited from folder or project properties, workspace preferences  
Autodetect: Automatic detection by juniversalchardet  
Content Type: Determined from content type setting  

## Installation
**Eclipse Marketplace**  
https://marketplace.eclipse.org/content/encoding-statusbar  
Drag install button of the above site to your running Eclipse workspace to install.  
<!--
**Update Site**  
Help > Install New Software...  
https://raw.githubusercontent.com/cypher256/eclipse-encoding-plugin/master/eclipse.encoding.plugin.update/site.xml
-->

## License
[Eclipse Public License - v 1.0](https://www.eclipse.org/legal/epl-v10.html)  
Copyright (c) 2016- Shinji Kashihara and others. All rights reserved.  
Original source: [ystsoi/eclipse-fileencodinginfo](https://github.com/ystsoi/eclipse-fileencodinginfo)
