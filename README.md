# Spoler
![Bygg og deploy](https://github.com/navikt/helse-spoler/workflows/Bygg%20og%20deploy/badge.svg)

## Beskrivelse
Spoler tilbake kafka offset. Kjører som en Job i Kubernetes ved deploy.

## How-to
1. Oppdater liste over app(er) som skal spoles tilbake (fjern de som evt ligger der fra sist kjøring) i [Application.kt](https://github.com/navikt/helse-spoler/blob/master/src/main/kotlin/no/nav/helse/spoler/Application.kt#L27)
1. Sørg for at appen(e) du skal spole tilbake ikke kjører. Vær obs på at k8s kan restarte ting. F.eks:
    1. Skaler ned replicas for respektive app(er): `kubectl scale --replicas=0 deployment <app>`, eller
    1. Slett deployment for apper som skal spoles tilbake
1. Kommenter inn relevante deployment steps i Spolers Github Actions workflow-fil
1. Commit og push
1. Kommenter ut deployment steps i Github actions
1. Commit og push
1. Redeploy eller skaler opp den eller de tilbakespolte appen(e)

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #team-bømlo-værsågod.
