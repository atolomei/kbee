package kbee.web.application;

import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;

public class MountedBeanMapper extends MountedMapper {

    public MountedBeanMapper(String path, PageResolver resolver) {
        super(path, () -> resolvePage(resolver));
    }

    private static Class<? extends IRequestablePage> resolvePage(PageResolver resolver) {
        return resolver.resolve(RequestCycle.get().getRequest());
    }
}