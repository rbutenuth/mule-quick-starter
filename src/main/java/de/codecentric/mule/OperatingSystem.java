package de.codecentric.mule;

public enum OperatingSystem {
	WINDOWS_X86 {
		@Override
		String getWrapperExecutable() {
			return "wrapper-windows-x86-64.exe";
		}

		@Override
		String getMavenExecutable() {
			return "mvn.cmd";
		}
	},
	LINUX_X86 {
		@Override
		String getWrapperExecutable() {
			return "wrapper-linux-x86-64";
		}

		@Override
		String getMavenExecutable() {
			return "mvn";
		}
	};
	
	abstract String getWrapperExecutable();
	
	abstract String getMavenExecutable();
	
	public static OperatingSystem determineOperatingSystem() {
		// possible values:
		//  - "Windows 10" / "amd64"
		//  - ??? (for Linux?)
		String name = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch");
		
		if (name.indexOf("windows") > -1 && arch.indexOf("64") > - 1) {
			return WINDOWS_X86;
		} else if (name.indexOf("linux") > -1 && arch.indexOf("64") > - 1) {
			return LINUX_X86;
		} else {
			throw new IllegalStateException("Don't know operating system " + name + " / " + arch);
		}
	}
}
