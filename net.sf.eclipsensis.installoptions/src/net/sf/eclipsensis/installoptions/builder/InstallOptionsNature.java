package net.sf.eclipsensis.installoptions.builder;

import java.util.Collection;
import java.util.HashSet;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

public class InstallOptionsNature implements IProjectNature 
{
	private IProject mProject;
    private Collection mEditedFiles = new HashSet();
    
    public void beginEditing(IFile file)
    {
        mEditedFiles.add(file);
        try {
            file.setPersistentProperty(IInstallOptionsConstants.RESOURCEPROPERTY_BUILD_TIMESTAMP,"-1"); //$NON-NLS-1$
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public void stopEditing(IFile file)
    {
        mEditedFiles.remove(file);
    }

	protected Collection getEditedFiles()
    {
        return mEditedFiles;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException 
    {
		IProjectDescription desc = mProject.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(IInstallOptionsConstants.INSTALLOPTIONS_BUILDER_ID)) {
				return;
			}
		}

		commands = (ICommand[])Common.resizeArray(commands,commands.length + 1);
		ICommand command = desc.newCommand();
		command.setBuilderName(IInstallOptionsConstants.INSTALLOPTIONS_BUILDER_ID);
        commands[commands.length - 1] = command;
		desc.setBuildSpec(commands);
		mProject.setDescription(desc, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException 
    {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(IInstallOptionsConstants.INSTALLOPTIONS_BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() 
    {
		return mProject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) 
    {
		mProject = project;
	}
    
	public static void addNature(IProject project)
    {
        try {
            IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();

            for (int i = 0; i < natures.length; ++i) {
                if (IInstallOptionsConstants.INSTALLOPTIONS_NATURE_ID.equals(natures[i])) {
                    return;
                }
            }

            // Add the nature
            natures = (String[])Common.resizeArray(natures,natures.length + 1);
            natures[natures.length-1] = IInstallOptionsConstants.INSTALLOPTIONS_NATURE_ID;
            description.setNatureIds(natures);
            project.setDescription(description, null);
            InstallOptionsBuilder.buildProject(project, IncrementalProjectBuilder.FULL_BUILD, null);
        } 
        catch (CoreException e) {
        }
    }
}
