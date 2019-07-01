/*
 * Copyright (c) 2019, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.gluonhq.attach;

import com.gluonhq.omega.attach.AttachService;
import com.gluonhq.omega.util.Constants;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttachServiceDefinition {

    private String name;
    private AttachService service;

    AttachServiceDefinition(String name) {
        this.name = name;

        try {
            this.service = AttachService.valueOf(name.replace('-', '_').toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String services = Stream.of(AttachService.values())
                    .map(AttachService::getServiceName)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Invalid name for Attach service: " + name +
                    " from list of services: " + services, e);
        }
    }

    AttachService getService() {
        return service;
    }

    String getSupportedPlatform(String target) {
        switch (target) {
            case Constants.TARGET_HOST:
                return getService().isDesktopSupported() ? "desktop" : "";
            case Constants.TARGET_IOS:
            case Constants.TARGET_IOS_SIM:
                return getService().isIosSupported() ? "ios" : "";
            default:
                throw new RuntimeException("No valid target found for " + target);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttachServiceDefinition that = (AttachServiceDefinition) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
