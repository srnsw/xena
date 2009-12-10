#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

#
# @(#)rpm-jh.spec	1.8 06/10/04
#
%prep
%ifos Linux
%ifarch %{arch}
    if [ ! -d /usr/src/redhat/RPMS/${arch} ]; then mkdir -p /usr/src/redhat/RPMS/${arch}; fi
    if [ ! -d /usr/src/redhat/SRPMS ]; then mkdir -p /usr/src/redhat/SRPMS; fi
    
%setup -n %{jh_dir}
    
%build
    
%install
	echo $RPM_BUILD_ROOT/%{jh_prefix}/%{jh_dir} 
    if [ -d $RPM_BUILD_ROOT/%{jh_prefix}/%{jh_dir} ]; then 
        rm -rf $RPM_BUILD_ROOT/%{jh_prefix}/%{jh_dir}
    fi

    mkdir -p $RPM_BUILD_ROOT%{jh_prefix}/%{jh_dir}
    cp -dpr $RPM_BUILD_DIR/%{jh_dir}/* \
            $RPM_BUILD_ROOT/%{jh_prefix}/%{jh_dir}/

%files
%defattr(-,root,root)
%{jh_prefix}/%{jh_dir}/*

%clean
    rm -rf $RPM_BUILD_DIR/%{jh_dir}
    rm -rf $RPM_BUILD_ROOT%{jh_prefix}/%{jh_dir}
%else
    echo "This package is for %{arch}."
    echo "To override add the /"--ignorearch/" option"
%endif
%else
    echo "This package is for the Linux operating system."
    echo "To override add the /"--ignoreos/" option"
%endif

%post
if [ $1 = 1 ] ; then
	umask 022

	cd %{jh_prefix}
	mkdir -p packages/lib/ext
	cd %{jh_prefix}/packages/lib/ext
	echo "Linking JavaHelp to %{jh_prefix}/packages/lib/ext ..."
	ln -s ../../../%{jh_dir}/%{jh_name}/lib/jhall.jar jhall.jar

	cd %{jh_prefix}
    	jdk_list=`ls -1d jdk* j2sdk* 2>/dev/null`
	for i in $jdk_list
	do 
		if [ -d %{jh_prefix}/$i/jre/lib/ext ]; then
			cd %{jh_prefix}/$i/jre/lib/ext  
			if [ ! -L jhall.jar ] ; then
				echo "Linking JavaHelp to %{jh_prefix}/$i ..."
				ln -s ../../../../%{jh_dir}/%{jh_name}/lib/jhall.jar jhall.jar 
			fi
		fi

		if [ -d %{jh_prefix}/$i/jre/bin ]; then
			cd %{jh_prefix}/$i/jre/bin  
			if [ ! -L jhsearch ] ; then
				ln -s ../../../%{jh_dir}/%{jh_name}/bin/jhsearch jhsearch
			fi

			if [ ! -L jhindexer ] ; then
				ln -s ../../../%{jh_dir}/%{jh_name}/bin/jhindexer jhindexer
			fi
        	fi

		if [ -d %{jh_prefix}/$i/bin ]; then
			cd %{jh_prefix}/$i/bin  
			if [ ! -L jhsearch ] ; then
				ln -s ../../%{jh_dir}/%{jh_name}/bin/jhsearch jhsearch
			fi
	
			if [ ! -L jhindexer ] ; then
				ln -s ../../%{jh_dir}/%{jh_name}/bin/jhindexer jhindexer
			fi
        	fi
	
	done 
	
	cd %{jh_prefix}
    	jre_list=`ls -1d jre* j2re* 2>/dev/null`
	for i in $jre_list
	do 
		if [ -d %{jh_prefix}/$i/lib/ext ]; then
			cd %{jh_prefix}/$i/lib/ext 
			if [ ! -L jhall.jar ] ; then
				echo "Linking JavaHelp to %{jh_prefix}/$i ..."
				ln -s ../../../%{jh_dir}/%{jh_name}/lib/jhall.jar jhall.jar
 			fi
        	fi
		
		if [ -d %{jh_prefix}/$i/bin ]; then
			cd %{jh_prefix}/$i/bin  
			if [ ! -L jhsearch ] ; then
				ln -s ../../%{jh_dir}/%{jh_name}/bin/jhsearch jhsearch
			fi

			if [ ! -L jhindexer ] ; then
				ln -s ../../%{jh_dir}/%{jh_name}/bin/jhindexer jhindexer
			fi
        	fi

	done 
fi


%preun

%postun
umask 022
rm -rf %{jh_prefix}/%{jh_dir}

        cd %{jh_prefix}
        jdk_list=`ls -1d jdk* j2sdk* 2>/dev/null`
        for i in $jdk_list
        do
                if [ -L %{jh_prefix}/$i/jre/lib/ext/jhall.jar ]; then
                       rm -f %{jh_prefix}/$i/jre/lib/ext/jhall.jar 
                fi
                
		if [ -L %{jh_prefix}/$i/jre/bin/jhindexer ]; then
                       rm -f %{jh_prefix}/$i/jre/bin/jhindexer 
                fi
                
		if [ -L %{jh_prefix}/$i/bin/jhindexer ]; then
                       rm -f %{jh_prefix}/$i/bin/jhindexer 
                fi
                
		if [ -L %{jh_prefix}/$i/jre/bin/jhsearch ]; then
                       rm -f %{jh_prefix}/$i/jre/bin/jhsearch 
                fi
                
		if [ -L %{jh_prefix}/$i/bin/jhsearch ]; then
                       rm -f %{jh_prefix}/$i/bin/jhsearch 
                fi
        done

        cd %{jh_prefix}
        jre_list=`ls -1d jre* j2re* 2>/dev/null`
        for i in $jre_list
        do
                if [ -L %{jh_prefix}/$i/lib/ext/jhall.jar ]; then
			rm -f %{jh_prefix}/$i/lib/ext/jhall.jar
                fi
                
		if [ -L %{jh_prefix}/$i/bin/jhindexer ]; then
                       rm -f %{jh_prefix}/$i/bin/jhindexer 
                fi
                
		if [ -L %{jh_prefix}/$i/bin/jhsearch ]; then
                       rm -f %{jh_prefix}/$i/bin/jhsearch 
                fi
        done
