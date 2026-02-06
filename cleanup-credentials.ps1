# PowerShell Script to Remove WiFi Credentials from Git History
# WARNING: This rewrites git history. Make sure you understand the consequences.

Write-Host "========================================" -ForegroundColor Red
Write-Host "  GIT HISTORY CREDENTIAL REMOVAL" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host ""
Write-Host "This script will remove WiFi credentials from your entire git history." -ForegroundColor Yellow
Write-Host "This is a DESTRUCTIVE operation that rewrites history." -ForegroundColor Yellow
Write-Host ""
Write-Host "Before proceeding:" -ForegroundColor Cyan
Write-Host "1. Make sure you've backed up any uncommitted work" -ForegroundColor White
Write-Host "2. Ensure all team members are aware (if applicable)" -ForegroundColor White
Write-Host "3. CHANGE YOUR WIFI PASSWORDS FIRST!" -ForegroundColor Red
Write-Host ""

$continue = Read-Host "Do you want to continue? (yes/no)"

if ($continue -ne "yes") {
    Write-Host "Aborting. No changes made." -ForegroundColor Green
    exit
}

Write-Host ""
Write-Host "Creating passwords.txt file..." -ForegroundColor Cyan

# Create passwords file with the exposed credentials
$passwords = @"
FailureToConnect
willywonka
TheMushroomHut
PurpleVersaceBicycle69
"@

$passwords | Out-File -FilePath "passwords.txt" -Encoding UTF8

Write-Host "Checking for git-filter-repo..." -ForegroundColor Cyan

# Check if git-filter-repo is installed
$filterRepo = Get-Command git-filter-repo -ErrorAction SilentlyContinue

if (-not $filterRepo) {
    Write-Host ""
    Write-Host "git-filter-repo is not installed." -ForegroundColor Red
    Write-Host ""
    Write-Host "Install options:" -ForegroundColor Yellow
    Write-Host "1. With Python/pip: pip install git-filter-repo" -ForegroundColor White
    Write-Host "2. Download from: https://github.com/newren/git-filter-repo" -ForegroundColor White
    Write-Host ""
    Write-Host "After installing, run this script again." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Running git-filter-repo..." -ForegroundColor Cyan
Write-Host "This may take a few moments..." -ForegroundColor Yellow
Write-Host ""

# Run git filter-repo to replace all instances of sensitive strings
git filter-repo --replace-text passwords.txt --force

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Successfully cleaned git history!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. Review the changes with: git log" -ForegroundColor White
    Write-Host "2. Force push to GitHub: git push origin main --force" -ForegroundColor White
    Write-Host ""
    Write-Host "WARNING: Force pushing will rewrite history on GitHub." -ForegroundColor Red
    Write-Host "Make sure no one else is working on this repository!" -ForegroundColor Red
    Write-Host ""

    $pushNow = Read-Host "Do you want to force push to GitHub now? (yes/no)"

    if ($pushNow -eq "yes") {
        Write-Host ""
        Write-Host "Force pushing to GitHub..." -ForegroundColor Yellow
        git push origin main --force

        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✓ Successfully pushed to GitHub!" -ForegroundColor Green
            Write-Host ""
            Write-Host "Credentials have been removed from git history." -ForegroundColor Green
            Write-Host "REMEMBER: Change your WiFi passwords if you haven't already!" -ForegroundColor Red
        } else {
            Write-Host ""
            Write-Host "✗ Push failed. Check your git remote configuration." -ForegroundColor Red
        }
    } else {
        Write-Host ""
        Write-Host "Skipping push. You can push later with:" -ForegroundColor Yellow
        Write-Host "git push origin main --force" -ForegroundColor White
    }

    # Clean up passwords file
    Remove-Item "passwords.txt" -ErrorAction SilentlyContinue
    Write-Host ""
    Write-Host "Cleaned up temporary files." -ForegroundColor Green

} else {
    Write-Host ""
    Write-Host "✗ git-filter-repo failed. Check the error messages above." -ForegroundColor Red
    Remove-Item "passwords.txt" -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan
Write-Host ""
