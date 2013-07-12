package org.apache.maven.plugins.enforcer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.plugins.enforcer.utils.EnforcerRuleUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * This rule checks that this pom or its parents don't define a repository.
 *
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class RequireNoRepositories
    extends AbstractNonCacheableEnforcerRule
{
    /**
     * Whether to ban non-plugin repositories. By default they are banned.
     * 
     * @deprecated the visibility will be reduced to private with the next major version
     * @see {@link #setBanRepositories(boolean)}
     */
    public boolean banRepositories = true;

    /**
     * Whether to ban plugin repositories. By default they are banned.
     * 
     * @deprecated the visibility will be reduced to private with the next major version
     * @see {@link #setBanPluginRepositories(boolean)}
     */
    public boolean banPluginRepositories = true;

    /**
     * Specify explicitly allowed non-plugin repositories. This is a list of ids.
     * 
     * @deprecated the visibility will be reduced to private with the next major version
     * @see {@link #setAllowedRepositories(List)}
     */
    public List<String> allowedRepositories = Collections.emptyList();

    /**
     * Specify explicitly allowed plugin repositories. This is a list of ids.
     * 
     * @deprecated the visibility will be reduced to private with the next major version
     * @see {@link #setAllowedPluginRepositories(List)}
     */
    public List<String> allowedPluginRepositories = Collections.emptyList();

    /**
     * Whether to allow repositories which only resolve snapshots. By default they are banned.
     * 
     * @deprecated the visibility will be reduced to private with the next major version
     * @see {@link #setAllowSnapshotRepositories(boolean)}
     */
    public boolean allowSnapshotRepositories = false;

    /**
     * Whether to allow plugin repositories which only resolve snapshots. By default they are banned.
     * 
     * @deprecated the visibility will be reduced to private with the next major version
     * @see {@link #setAllowSnapshotPluginRepositories(boolean)}
     */
    public boolean allowSnapshotPluginRepositories = false;

    public final void setBanRepositories( boolean banRepositories )
    {
        this.banRepositories = banRepositories;
    }
    
    public final void setBanPluginRepositories( boolean banPluginRepositories )
    {
        this.banPluginRepositories = banPluginRepositories;
    }
    
    public final void setAllowedRepositories( List<String> allowedRepositories )
    {
        this.allowedRepositories = allowedRepositories;
    }
    
    public final void setAllowedPluginRepositories( List<String> allowedPluginRepositories )
    {
        this.allowedPluginRepositories = allowedPluginRepositories;
    }
    
    public final void setAllowSnapshotRepositories( boolean allowSnapshotRepositories )
    {
        this.allowSnapshotRepositories = allowSnapshotRepositories;
    }
    
    public final void setAllowSnapshotPluginRepositories( boolean allowSnapshotPluginRepositories )
    {
        this.allowSnapshotPluginRepositories = allowSnapshotPluginRepositories;
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.apache.maven.enforcer.rule.api.EnforcerRule#execute(org.apache.maven.enforcer.rule.api.EnforcerRuleHelper)
     */
    public void execute( EnforcerRuleHelper helper )
        throws EnforcerRuleException
    {
        EnforcerRuleUtils utils = new EnforcerRuleUtils( helper );

        MavenProject project;
        try
        {
            project = (MavenProject) helper.evaluate( "${project}" );

            List<Model> models =
                utils.getModelsRecursively( project.getGroupId(), project.getArtifactId(), project.getVersion(),
                                            new File( project.getBasedir(), "pom.xml" ) );
            List<Model> badModels = new ArrayList<Model>();

            StringBuffer newMsg = new StringBuffer();
            newMsg.append( "Some poms have repositories defined:\n" );

            for ( Model model : models )
            {
                if ( banRepositories )
                {
                    @SuppressWarnings( "unchecked" )
                    List<Repository> repos = model.getRepositories();
                    if ( repos != null && !repos.isEmpty() )
                    {
                        List<String> bannedRepos =
                            findBannedRepositories( repos, allowedRepositories, allowSnapshotRepositories );
                        if ( !bannedRepos.isEmpty() )
                        {
                            badModels.add( model );
                            newMsg.append(
                                model.getGroupId() + ":" + model.getArtifactId() + " version:" + model.getVersion()
                                    + " has repositories " + bannedRepos );
                        }
                    }
                }
                if ( banPluginRepositories )
                {
                    @SuppressWarnings( "unchecked" )
                    List<Repository> repos = model.getPluginRepositories();
                    if ( repos != null && !repos.isEmpty() )
                    {
                        List<String> bannedRepos =
                            findBannedRepositories( repos, allowedPluginRepositories, allowSnapshotPluginRepositories );
                        if ( !bannedRepos.isEmpty() )
                        {
                            badModels.add( model );
                            newMsg.append(
                                model.getGroupId() + ":" + model.getArtifactId() + " version:" + model.getVersion()
                                    + " has plugin repositories " + bannedRepos );
                        }
                    }
                }
            }

            // if anything was found, log it then append the
            // optional message.
            if ( !badModels.isEmpty() )
            {
                String message = getMessage();
                if ( StringUtils.isNotEmpty( message ) )
                {
                    newMsg.append( message );
                }

                throw new EnforcerRuleException( newMsg.toString() );
            }

        }
        catch ( ExpressionEvaluationException e )
        {
            throw new EnforcerRuleException( e.getLocalizedMessage() );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new EnforcerRuleException( e.getLocalizedMessage() );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new EnforcerRuleException( e.getLocalizedMessage() );
        }
        catch ( IOException e )
        {
            throw new EnforcerRuleException( e.getLocalizedMessage() );
        }
        catch ( XmlPullParserException e )
        {
            throw new EnforcerRuleException( e.getLocalizedMessage() );
        }
    }

    /**
     * 
     * @param repos all repositories, never {@code null}
     * @param allowedRepos allowed repositories, never {@code null}
     * @param allowSnapshots 
     * @return
     */
    private static List<String> findBannedRepositories( List<Repository> repos, List<String> allowedRepos, boolean allowSnapshots )
    {
        List<String> bannedRepos = new ArrayList<String>( allowedRepos.size() );
        for ( Repository r : repos )
        {
            if ( !allowedRepos.contains( r.getId() ) )
            {
                if ( !allowSnapshots || r.getReleases() == null || r.getReleases().isEnabled() )
                {
                    // if we are not allowing snapshots and this repo is enabled for releases
                    // it is banned.  We don't care whether it is enabled for snapshots
                    // if you define a repo and don't enable it for anything, then we have nothing 
                    // to worry about
                    bannedRepos.add( r.getId() );
                }
            }
        }
        return bannedRepos;
    }
}