import { handler as frontend } from '../../01_frontend/.output/server/index.mjs';

export const handler = async (event, _context) => {
    return frontend(event, _context);
}