# Remove WiFi Credentials from Git History

## ⚠️ IMPORTANT SECURITY NOTICE

Your WiFi credentials were previously committed to git and pushed to GitHub. Even though we've removed them from the current files, they still exist in the git history and are publicly visible on GitHub.

## What was exposed:
- SSID: "FailureToConnect" / Password: "willywonka" (sketch_jan23a)
- SSID: "TheMushroomHut" / Password: "PurpleVersaceBicycle69" (fractal)

## Immediate Actions Required:

### 1. Change Your WiFi Passwords
**Do this first!** Even after removing from git, the credentials were public. Change your WiFi passwords immediately on your router.

### 2. Remove Credentials from Git History

We need to rewrite git history to remove these credentials. Here are your options:

#### Option A: Using git filter-repo (Recommended)

1. Install git-filter-repo:
   ```bash
   pip install git-filter-repo
   ```

2. Create a file called `passwords.txt` with the sensitive strings (one per line):
   ```
   FailureToConnect
   willywonka
   TheMushroomHut
   PurpleVersaceBicycle69
   ```

3. Run the filter:
   ```bash
   git filter-repo --replace-text passwords.txt
   ```

4. Force push to GitHub:
   ```bash
   git push origin main --force
   ```

#### Option B: Using BFG Repo-Cleaner (Faster for large repos)

1. Download BFG: https://rtyley.github.io/bfg-repo-cleaner/

2. Create `passwords.txt` as above

3. Run BFG:
   ```bash
   java -jar bfg.jar --replace-text passwords.txt
   ```

4. Clean up and force push:
   ```bash
   git reflog expire --expire=now --all
   git gc --prune=now --aggressive
   git push origin main --force
   ```

#### Option C: Nuclear Option - Delete and Re-create Repository

If you want to completely start fresh:

1. Delete the GitHub repository
2. Create a new empty repository with the same name
3. Push the current clean version:
   ```bash
   git push origin main --force
   ```

## After Cleanup

1. ✅ Verify credentials are gone from GitHub history
2. ✅ Confirm WiFi passwords have been changed on your router
3. ✅ Add your new credentials locally (don't commit them!)
4. ✅ Double-check .gitignore is working

## Prevention

Going forward:
- Never commit credentials directly in code
- Use environment variables or config files (in .gitignore)
- Consider using Arduino secrets library
- Review commits before pushing
