/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers.registry;

import com.github.jknack.handlebars.Decorator;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.HelperRegistry;
import com.github.jknack.handlebars.helper.BlockHelper;
import com.github.jknack.handlebars.helper.EachHelper;
import com.github.jknack.handlebars.helper.IfHelper;
import com.github.jknack.handlebars.helper.InlineDecorator;
import com.github.jknack.handlebars.helper.LogHelper;
import com.github.jknack.handlebars.helper.LookupHelper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.helper.UnlessHelper;
import com.github.jknack.handlebars.helper.WithHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.CssHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.DefinePlaceholderHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.DefineZoneHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.FaviconHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.FillZoneHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.FragmentHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.HeadJsHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.HeadOtherHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.I18nHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.JsHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.MenuHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.MissingHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.PublicHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.SecuredHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.TemplateHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.TitleHelper;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link HelperRegistry} used in UUF.
 * Reusing the some code from {@link com.github.jknack.handlebars.helper.DefaultHelperRegistry} and additionally
 * including helpers written for UUF.
 *
 * @since 1.0.0
 */
public class HbsHelperRegistry implements HelperRegistry {

    private final Logger logger = LoggerFactory.getLogger(HbsHelperRegistry.class);

    /**
     * The helper registry.
     */
    private final Map<String, Helper<?>> helpers = new HashMap<>();

    /**
     * Decorators.
     */
    private final Map<String, Decorator> decorators = new HashMap<>();

    /**
     * Default constructor that registers all the default hbs helpers and additionally registers UUF related helpers
     */
    public HbsHelperRegistry() {
        registerDefaultHelpers(this);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <C> Helper<C> helper(final String name) {
        return (Helper<C>) helpers.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <H> HelperRegistry registerHelper(final String name, final Helper<H> helper) {
        Helper<?> oldHelper = helpers.put(name, helper);
        if (oldHelper != null) {
            logger.warn("Helper '{}' has been replaced by '{}'", name, helper);
        }
        return this;
    }

    /**
     * This method is not supported and not used within UUF.
     */
    @Override
    public <H> HelperRegistry registerHelperMissing(final Helper<H> helper) {
        return registerHelper(Handlebars.HELPER_MISSING, helper);
    }

    /**
     * This method is not supported and not used within UUF.
     */
    @Override
    public HelperRegistry registerHelpers(final Object helperSource) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * This will register only the class source of enum with {@link Helper} implementation.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public HelperRegistry registerHelpers(final Class<?> helperSource) {
        if (!Enum.class.isAssignableFrom(helperSource)) {
            throw new UnsupportedOperationException();
        }
        Enum[] helpers = ((Class<Enum>) helperSource).getEnumConstants();
        for (Enum helper : helpers) {
            registerHelper(helper.name(), (Helper) helper);
        }
        return this;
    }

    /**
     * This method is not supported and not used within UUF.
     */
    @Override
    public HelperRegistry registerHelpers(final URI location) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported and not used within UUF.
     */
    @Override
    public HelperRegistry registerHelpers(final File input) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported and not used within UUF.
     */
    @Override
    public HelperRegistry registerHelpers(final String filename, final Reader source)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported and not used within UUF.
     */
    @Override
    public HelperRegistry registerHelpers(final String filename, final InputStream source)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported and not used within UUF.
     */
    @Override
    public HelperRegistry registerHelpers(final String filename, final String source)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Map.Entry<String, Helper<?>>> helpers() {
        return this.helpers.entrySet();
    }


    /**
     * Register built-in and default helpers. We are not registering some of the unwanted helpers (partial, embedded,
     * i18n. etc) as they are replaced by the custom helpers written for UUF.
     *
     * @param registry The handlebars instance.
     */
    private void registerDefaultHelpers(final HelperRegistry registry) {
        registry.registerHelper(WithHelper.NAME, WithHelper.INSTANCE);
        registry.registerHelper(IfHelper.NAME, IfHelper.INSTANCE);
        registry.registerHelper(UnlessHelper.NAME, UnlessHelper.INSTANCE);
        registry.registerHelper(EachHelper.NAME, EachHelper.INSTANCE);
        registry.registerHelper(BlockHelper.NAME, BlockHelper.INSTANCE);
        registry.registerHelper(LookupHelper.NAME, LookupHelper.INSTANCE);
        registry.registerHelper(LogHelper.NAME, LogHelper.INSTANCE);
        registry.registerHelpers(StringHelpers.class);
        //UUF related helpers
        registry.registerHelper(FragmentHelper.HELPER_NAME, new FragmentHelper());
        registry.registerHelper(SecuredHelper.HELPER_NAME, new SecuredHelper());
        registry.registerHelper(PublicHelper.HELPER_NAME, new PublicHelper());
        registry.registerHelper(MenuHelper.HELPER_NAME, new MenuHelper());
        registry.registerHelper(DefineZoneHelper.HELPER_NAME, new DefineZoneHelper());
        registry.registerHelper(FillZoneHelper.HELPER_NAME, new FillZoneHelper());
        registry.registerHelper(DefinePlaceholderHelper.HELPER_NAME, new DefinePlaceholderHelper());
        registry.registerHelper(FaviconHelper.HELPER_NAME, new FaviconHelper());
        registry.registerHelper(TitleHelper.HELPER_NAME, new TitleHelper());
        registry.registerHelper(CssHelper.HELPER_NAME, new CssHelper());
        registry.registerHelper(HeadJsHelper.HELPER_NAME, new HeadJsHelper());
        registry.registerHelper(HeadOtherHelper.HELPER_NAME, new HeadOtherHelper());
        registry.registerHelper(JsHelper.HELPER_NAME, new JsHelper());
        registry.registerHelper(I18nHelper.HELPER_NAME, new I18nHelper());
        registry.registerHelper(TemplateHelper.HELPER_NAME, new TemplateHelper());
        registry.registerHelperMissing(new MissingHelper());
        // decorator
        registry.registerDecorator("inline", InlineDecorator.INSTANCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decorator decorator(final String name) {
        return decorators.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HelperRegistry registerDecorator(final String name, final Decorator decorator) {
        Decorator old = decorators.put(name, decorator);
        if (old != null) {
            logger.warn("Decorator '{}' has been replaced by '{}'", name, decorator);
        }
        return this;
    }
}
