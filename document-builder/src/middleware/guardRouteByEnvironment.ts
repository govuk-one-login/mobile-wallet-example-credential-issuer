import { NextFunction, Request, Response } from "express";
import { getSelfUrl, getEnvironment } from "../config/appConfig";
import { commonRoutes, dvsRoutes, gdsRoutes, ROUTES } from "../config/routes";
import { allDvsRoutesEnvs, gdsRoutesEnvs } from "../config/environments";

const matchRoutesWithParam = (routeToTest: string, currentRoute: string) => {
  if (routeToTest.includes(":")) {
    // Convert Express route parameters (e.g. :credentialType) to regex pattern [^/]+ to match any non-slash characters
    const pattern = routeToTest.replaceAll(/:[^/]+/g, "[^/]+");
    return new RegExp(`^${pattern}$`).test(currentRoute);
  }

  return (
    routeToTest === currentRoute ||
    routeToTest === currentRoute.replace(/\/$/, "") // remove the trailing slash
  );
};

export function guardRouteByEnvironment(
  selfUrl = getSelfUrl(),
  environment = getEnvironment(),
) {
  return function (req: Request, res: Response, next: NextFunction) {
    const currentRoute = req.path;

    const isDvsRoute = dvsRoutes.some((route) =>
      matchRoutesWithParam(route, currentRoute),
    );
    const isGdsRoute = gdsRoutes.some((route) =>
      matchRoutesWithParam(route, currentRoute),
    );
    const isCommonRoute = commonRoutes.some((route) =>
      matchRoutesWithParam(route, currentRoute),
    );

    const isDvsEnvironment = allDvsRoutesEnvs.includes(environment);
    const isIssuerEnvironment = gdsRoutesEnvs.includes(environment);

    if (isCommonRoute) {
      next();
    } else if (isDvsRoute && isDvsEnvironment) {
      next();
    } else if (isGdsRoute && isIssuerEnvironment) {
      next();
    } else if (isIssuerEnvironment) {
      res.redirect(selfUrl + ROUTES.START);
    } else if (isDvsEnvironment) {
      res.redirect(selfUrl + ROUTES.DVS_START);
    } else {
      res.redirect("/");
    }
  };
}
